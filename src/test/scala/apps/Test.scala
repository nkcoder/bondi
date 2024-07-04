package io.daniel
package apps

import aws.EmailService

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.all.*

object Test extends IOApp {
  val body =
    """
      |<html>
      |<head></head>
      |<body><p>Hello,</p>
      |<p>Please find attached the club transfer data for your club.</p>
      |<p>Regards,</p>
      |</html>
      |""".stripMargin

  override def run(args: List[String]): IO[ExitCode] = {
    for {
//      _ <- sendEmail()
      _ <- readData()
    } yield ExitCode.Success
  }

  def readData(): IO[Unit] =
    for {
      data          <- ClubTransfer.readClubTransferData()
      _             <- IO.println(s"Data: $data")
      immutableData <- ClubTransfer.readClubTransferDataImmutable()
      _             <- IO.println(s"Immutable data: $immutableData")
      - <- data.keys.toList.traverse_ { case club =>
        val clubData  = data(club)
        val clubData2 = immutableData(club)
        IO.println(
          s"club data size: ${clubData.length}, club data 2 size: ${clubData2.length}, equal? ${clubData == clubData2}"
        )
      }
    } yield ()

  def sendEmail(): IO[Unit] = {
    for {
      _ <- EmailService.sendEmailWithAttachment(
        sender = "noreply@plus.fitness",
        recipient = "daniel.guo@vivalabs.com.au",
        subject = "Test for DD Club Transfer",
        body = body,
        "dd_club_transfer_WILEY PARK.csv"
      )
    } yield ()
  }
}
