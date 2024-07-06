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

object ClubTransfer extends IOApp {

  val sender           = "noreply@the-hub.ai"
  val subject          = "Club Transfer for Direct Debit Members"
  private val toDaniel = "daniel.guo@vivalabs.com.au"
  val body =
    """
      |<html>
      |<head></head>
      |<body><p>Hello,</p>
      |<p>Please find attached the Direct Debit club transfer data for your club (April - June 2024).</p>
      |<p>Regards</p>
      |</html>
      |""".stripMargin

  private def readClubTransferData(): IO[Map[String, List[ClubTransferData]]] = IO {
    val clubTransferInputData = CSVReader.open("dd_club_transfer.csv").allWithHeaders()
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

  private def writeToCsvFile(data: Map[String, List[ClubTransferData]]): IO[Unit] = IO {
    data.foreach { case (club, transfers) =>
      val writer = CSVWriter.open(s"dd_club_transfer_$club.csv")
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
            transferType,
            transferDate.toString
          )
        )
      }

      writer.close()
    }
  }

  private def sendEmailToClub(clubs: List[String], session: Session[IO]): IO[Unit] = {
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
              val fileName = s"dd_club_transfer_$clubName.csv"
//              EmailService.sendEmailWithAttachment(sender, email, subject, body, fileName)
              EmailService.sendEmailWithAttachment(sender, toDaniel, subject, body, fileName)
            case None =>
              IO.println(s"Location not found for club: $clubName")
            case _ =>
              IO.println(s"--- Email not found for club: $clubName")
          }
          _ <- IO.println(s"Process club: $clubName completed")
          _ <- IO.sleep(1.second)
        } yield ()
      }
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val env = Properties.envOrElse("APP_ENV", "local")
    DbConfig
      .load(env)
      .fold(
        error => IO(println(error)).as(ExitCode.Error),
        config => {
          DbConnection.pooled[IO](config).use { resource =>
            resource.use { session =>
              for {
                locationRepository <- LocationRepository.make(session)
                data               <- readClubTransferData()
                _                  <- writeToCsvFile(data)
                _                  <- sendEmailToClub(data.keys.toList, session)
              } yield ExitCode.Success
            }
          }
        }
      )
  }
}
