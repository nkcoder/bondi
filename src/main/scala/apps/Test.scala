package io.daniel
package apps

import aws.EmailService

import cats.effect.{ExitCode, IO, IOApp}

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
      _ <- EmailService.send(
        sender = "noreply@plus.fitness",
        recipient = "daniel.guo@vivalabs.com.au",
        subject = "Test",
        body = body,
        "club_transfer_ALBION PARK.csv"
      )
    } yield ExitCode.Success
  }
}
