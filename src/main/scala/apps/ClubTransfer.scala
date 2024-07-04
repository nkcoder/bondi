package io.daniel
package apps

import aws.EmailService
import db.{DbConfig, DbConnection}
import domain.location.LocationRepository

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*
import com.github.tototoshi.csv.{CSVReader, CSVWriter}
import natchez.Trace.Implicits.noop
import skunk.Session

import java.time.LocalDate
import scala.collection.mutable
import scala.util.Properties

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
    membershipType: String,
    homeClub: String,
    targetClub: String,
    visitCount: Int,
    totalVisits: Int
)

case class ClubTransferData(
    memberId: String,
    fobNumber: String,
    firstName: String,
    surname: String,
    membershipType: String,
    homeClub: String,
    targetClub: String,
    transferType: String,
    transferDate: LocalDate
)

object ClubTransfer extends IOApp {

  val sender   = "noreply@the-hub.ai"
  val subject  = "Club Transfer for PIF Members"
  val toDaniel = "daniel.guo@vivalabs.com.au"
  val body =
    """
      |<html>
      |<head></head>
      |<body><p>Hello,</p>
      |<p>Please find attached the PIF club transfer data for your club.</p>
      |<p>Regards</p>
      |</html>
      |""".stripMargin

  private def readClubTransferData(): IO[Map[String, List[ClubTransferData]]] = IO {
    val clubTransferInputData = CSVReader.open("pif_club_transfer.csv").allWithHeaders()
    val clubTransferRows = clubTransferInputData.map { row =>
      ClubTransferRow(
        row("memberId"),
        row("fobNumber"),
        row("firstName"),
        row("surname"),
        row("membershipType"),
        row("homeClub"),
        row("targetClub"),
        row("visitCount").toInt,
        row("totalVisits").toInt
      )
    }

    val result = mutable.Map[String, List[ClubTransferData]]()

    clubTransferRows.foreach(row => {
      val transferIn = ClubTransferData(
        row.memberId,
        row.fobNumber,
        row.firstName,
        row.surname,
        row.membershipType,
        row.homeClub,
        row.targetClub,
        "TRANSFER IN",
        LocalDate.now()
      )

      result.get(row.targetClub) match {
        case Some(list) => result(row.targetClub) = result(row.targetClub) :+ transferIn
        case None       => result(row.targetClub) = List(transferIn)
      }

      val transferOut = transferIn.copy(transferType = "TRANSFER OUT")
      result.get(row.homeClub) match {
        case Some(list) => result(row.homeClub) = result(row.homeClub) :+ transferOut
        case None       => result(row.homeClub) = List(transferOut)
      }
    })

    result.toMap
  }

  private def writeToCsvFile(data: Map[String, List[ClubTransferData]]): IO[Unit] = IO {
    data.foreach { case (club, transfers) =>
      val writer = CSVWriter.open(s"pif_club_transfer_$club.csv")
      writer.writeRow(
        List(
          "Member ID",
          "FOB Number",
          "First Name",
          "Surname",
          "Membership Type",
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
            membershipType,
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
              val fileName = s"pif_club_transfer_$clubName.csv"
              IO.println(s"Sending email to: $email, fileName: $fileName")
//              EmailService.send(sender, email, subject, body, fileName)
              EmailService.sendEmailWithAttachment(sender, toDaniel, subject, body, fileName)
            case None =>
              IO.println(s"Location not found for club: $clubName")
            case _ =>
              IO.println(s"--- Email not found for club: $clubName")
          }
          _ <- IO.println(s"Process club: $clubName completed")
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
