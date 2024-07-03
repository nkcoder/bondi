package io.daniel
package apps

import db.DbConfig

import cats.effect.{ExitCode, IO, IOApp}
import com.github.tototoshi.csv.{CSVReader, CSVWriter}

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
      println(s"club: $club, transfers: $transfers")
      val writer = CSVWriter.open(s"club_transfer_$club.csv")
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

  override def run(args: List[String]): IO[ExitCode] = {
    val env = Properties.envOrElse("APP_ENV", "local")
    DbConfig
      .load(env)
      .fold(
        error => IO(println(error)).as(ExitCode.Error),
        config => {
          for {
            data <- readClubTransferData()
            _    <- IO(println(data))
            _    <- writeToCsvFile(data)
          } yield ExitCode.Success
        }
      )
  }

}
