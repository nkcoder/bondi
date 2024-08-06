package io.daniel
package apps

import java.time.LocalDate

import scala.concurrent.duration.DurationInt
import scala.util.Properties

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.all._
import com.github.tototoshi.csv.CSVReader
import com.github.tototoshi.csv.CSVWriter
import natchez.Trace.Implicits.noop
import skunk.Session

import apps.PaymentType.{DD, PIF}
import aws.EmailService
import db.{DbConfig, DbConnection}
import domain.location.LocationRepository

/** Use the scala-csv library.
  *
  * Read club transfer data from a csv file, generate club transfer transactions, and write the output to a csv file for
  * each club. Finally, send an email to each club with the club transfer transactions.
  */

case class ClubTransferRow(
    memberId: String,
    fobNumber: String,
    firstName: String,
    surname: String,
    homeClub: String,
    targetClub: String
)

case class ClubTransferData(
    memberId: String,
    fobNumber: String,
    firstName: String,
    surname: String,
    homeClub: String,
    targetClub: String,
    transferType: String,
    transferDate: LocalDate
)

enum PaymentType:
  case PIF, DD

/** Be careful running the application, because it will send **real** emails to the clubs.
  */
object ClubTransfer extends IOApp {

  private def readClubTransferData(paymentType: PaymentType): IO[Map[String, List[ClubTransferData]]] = IO {
    val transferFileName      = buildFileName(paymentType, None)
    val clubTransferInputData = CSVReader.open(transferFileName).allWithHeaders()
    val clubTransferRows = clubTransferInputData.map { row =>
      ClubTransferRow(
        row("memberId"),
        row("fobNumber"),
        row("firstName"),
        row("surname"),
        row("homeClub").toUpperCase,
        row("targetClub").toUpperCase
      )
    }

    val transfers = clubTransferRows.flatMap { row =>
      val transferIn = ClubTransferData(
        row.memberId,
        row.fobNumber,
        row.firstName,
        row.surname,
        row.homeClub,
        row.targetClub,
        "TRANSFER IN",
        LocalDate.now()
      )

      val transferOut = transferIn.copy(transferType = "TRANSFER OUT")
      List(row.targetClub -> transferIn, row.homeClub -> transferOut)
    }

    transfers.groupMap(_._1)(_._2)
  }

  private def writeToCsvFile(data: Map[String, List[ClubTransferData]], paymentType: PaymentType): IO[Unit] = IO {
    data.foreach { case (club, transfers) =>
      val clubFileName = buildFileName(paymentType, Some(club))
      val writer       = CSVWriter.open(clubFileName)
      writer.writeRow(
        List(
          "Member ID",
          "FOB Number",
          "First Name",
          "Surname",
          "Home Club",
          "Target Club",
          "Transfer Type",
          "Transfer Date"
        )
      )
      transfers.foreach { transfer =>
        import transfer.*
        writer.writeRow(
          List(
            memberId,
            fobNumber,
            firstName,
            surname,
            homeClub,
            targetClub,
            paymentType,
            transferDate.toString
          )
        )
      }

      writer.close()
    }
  }

  private def sendEmailToClub(clubs: List[String], session: Session[IO], paymentType: PaymentType): IO[Unit] = {
    val sender = "noreply@the-hub.ai"

    val lastMonth = LocalDate.now().minusMonths(1).getMonth
    val (subject, bodyContent) = paymentType match
      case PIF =>
        (
          "Club Transfer for Paid in Full Members",
          s"Please find attached the Paid in Full club transfer data for your club ($lastMonth 2024)."
        )
      case DD =>
        val lastQuarter = LocalDate.now().minusMonths(3).getMonth
        (
          "Club Transfer for Direct Debit Members",
          s"Please find attached the Direct Debit club transfer data for your club ($lastQuarter - $lastMonth 2024)."
        )

    val toDaniel = "daniel.guo@vivalabs.com.au"
    val body =
      s"""
        |<html>
        |<head></head>
        |<body><p>Hello team,</p>
        |<p>$bodyContent</p>
        |<p>Regards</p>
        |</html>
        |""".stripMargin

    for {
      _                  <- IO.println(s"Total: ${clubs.length} clubs")
      locationRepository <- LocationRepository.make(session)
      _ <- clubs.traverse_ { clubName =>
        for {
          _             <- IO.println(s"Processing club: $clubName")
          maybeLocation <- locationRepository.findByName(clubName)
          _ <- maybeLocation match {
            case Some(location) if location.email.isDefined =>
              val email = location.email.get
              println(s"Location email: $email")
              val clubTransferFile = buildFileName(paymentType, Some(clubName))
//              EmailService.sendEmailWithAttachment(sender, email, subject, body, fileName)
              EmailService.sendEmailWithAttachment(sender, toDaniel, subject, body, clubTransferFile)
            case None =>
              IO.println(s"--- Location not found for club: $clubName ---")
            case _ =>
              IO.println(s"--- Email not found for club: $clubName ---")
          }
          _ <- IO.println(s"Process club: $clubName completed")
          _ <- IO.sleep(1.second)
        } yield ()
      }
    } yield ()
  }

  private def buildFileName(paymentType: PaymentType, maybeSuffix: Option[String]): String =
    val suffix = maybeSuffix.fold("")(name => s"_$name")
    paymentType match {
      case DD  => s"dd_club_transfer$suffix.csv"
      case PIF => s"pif_club_transfer$suffix.csv"
    }

  /** <p> How to run the application:
    *   - change the `paymentType`
    *   - put the corresponding csv file in the `root` folder
    *   - run the application: auto/prod io.daniel.apps.ClubTransfer </p>
    */
  override def run(args: List[String]): IO[ExitCode] = {
    val env         = Properties.envOrElse("APP_ENV", "local")
    val paymentType = PaymentType.DD

    DbConfig
      .load(env)
      .fold(
        error => IO(println(error)).as(ExitCode.Error),
        config => {
          DbConnection.pooled[IO](config).use { resource =>
            resource.use { session =>
              for {
                locationRepository <- LocationRepository.make(session)
                data               <- readClubTransferData(paymentType)
                _                  <- writeToCsvFile(data, paymentType)
                _                  <- sendEmailToClub(data.keys.toList, session, paymentType)
              } yield ExitCode.Success
            }
          }
        }
      )
  }
}
