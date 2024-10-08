package io.daniel
package apps

import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID

import scala.io.Source
import scala.util.Properties

import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import natchez.Trace.Implicits.noop
import skunk._

import db.{DbConfig, DbConnection}
import domain.billing.BillingRepository
import domain.member.MemberRepository
import domain.refund.Refund
import support.DateTimeUtil

final case class InputItem(
    name: String,
    email: String,
    ddDate: String,
    bsb: String,
    accountNumber: Long,
    reason: String,
    amount: BigDecimal,
    refundDate: String
)

/** RefundApp reads input records from a file, generates refund transactions, and writes the output to a file.
  */
object RefundApp extends IOApp.Simple:
  def run: IO[Unit] =
    val env = Properties.envOrElse("APP_ENV", "local")
    DbConfig
      .load(env)
      .fold(
        error => IO(println(error)),
        config => doProcessRefund(config)
      )

  private def generateRefundTransaction(inputRecord: InputItem, session: Session[IO]): IO[Option[Refund]] =
    for {
      memberRepo   <- MemberRepository.make(session)
      memberOption <- memberRepo.findByEmail(inputRecord.email)
      billingRepo  <- BillingRepository.make(session)
      debitDate = if inputRecord.ddDate.isEmpty then inputRecord.refundDate else inputRecord.ddDate
      billingOption <- memberOption match {
        case Some(member) =>
          println(s"Member found for email: ${inputRecord.email}")
          billingRepo.findByMemberIdAndDebitDate(member.id, debitDate)
        case None =>
          println(s"[WARN] Member not found for email: ${inputRecord.email}")
          IO.pure(None)
      }
      result <- (memberOption, billingOption) match
        case (Some(member), Some(billing)) =>
          println(s"Billing found for member: ${member.id}, debitDate: $debitDate")
          for {
            nowInUTC <- DateTimeUtil.nowInUTC
          } yield Some(
            Refund(
              id = UUID.randomUUID().toString,
              billingId = billing.id,
              bsb = inputRecord.bsb,
              accountName = inputRecord.name,
              accountNumber = String.valueOf(inputRecord.accountNumber),
              locationId = billing.locationId,
              memberId = member.id,
              refundAmount = inputRecord.amount,
              refundReason = "MANUAL" + "_" + inputRecord.reason,
              createdAt = nowInUTC,
              updatedAt = nowInUTC
            )
          )
        case (Some(member), None) =>
          println(
            s"--- [WARN] No billing found for member: ${member.id}, email: ${inputRecord.email}, debitDate: $debitDate"
          )
          IO.pure(None)
        case _ => IO.pure(None)
    } yield result

  private def readInputRecords: IO[List[InputItem]] =
    IO {
      val source      = Source.fromFile("refund_input.json")
      val fileContent = source.mkString
      source.close()

      val inputRecords = parser.parse(fileContent).flatMap(_.as[List[InputItem]]) match
        case Right(records) => records
        case Left(error)    => throw new RuntimeException(s"Failed to parse input file: $error")

      inputRecords
    }

  private def saveToFile(data: String): IO[Unit] =
    IO {
      val file = Paths.get("refund_output.json")
      Files.write(file, data.getBytes())
    }

  private def doProcessRefund(dbConfig: DbConfig): IO[Unit] =
    DbConnection.pooled[IO](dbConfig).use { resource =>
      resource.use { session =>
        for {
          allRecords         <- readInputRecords
          _                  <- IO.println(s"Read ${allRecords.size} records from input file")
          refundTransactions <- allRecords.traverse(inputItem => generateRefundTransaction(inputItem, session))
          _                  <- saveToFile(refundTransactions.flatten.asJson.spaces2)
          _ = println("Output file generated successfully!")
        } yield ()
      }
    }
