package io.daniel
package apps

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp

import aws.EmailService

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
      _ <- sendEmail()
    } yield ExitCode.Success
  }
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
