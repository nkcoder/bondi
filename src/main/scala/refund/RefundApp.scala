package io.daniel
package refund

import refund.*

import cats.effect.*
import cats.implicits.*
import io.circe.generic.auto.*
import io.circe.parser
import io.circe.syntax.*
import skunk.*
import skunk.codec.all.*
import skunk.implicits.*

import java.nio.file.{Files, Paths}
import java.util.UUID
import scala.io.Source

object RefundApp extends IOApp {

  private val session: Resource[IO, Session[IO]] = SessionManager.prod

  private val getMember: Query[String, Member] = sql"select id from member where email = $varchar".query(memberDecoder)
  private val getBilling: Query[String *: String *: EmptyTuple, Billing] =
    sql"select id, location_id from member_contract_billing mcb where member_id = $varchar and debit_date = $varchar::timestamp with time zone"
      .query(billingDecoder)

  private def generateRefundTransaction(inputRecord: InputItem): IO[RefundTransaction] = session.use { s =>
    for {
      getMemberQuery <- s.prepare(getMember)
      member <- getMemberQuery
        .option(inputRecord.email)
        .flatMap {
          case Some(member) =>
            println(s"Member found for email: ${inputRecord.email}")
            IO.pure(member)
          case None => IO.raiseError(new RuntimeException(s"Member not found for email: ${inputRecord.email}"))
        }
      getBillingQuery <- s.prepare(getBilling)
      debitDate = if inputRecord.ddDate.isEmpty then inputRecord.refundDate else inputRecord.ddDate
      billing <- getBillingQuery.option(member.id, debitDate).flatMap {
        case Some(billing) =>
          println(s"Billing found for member: ${member.id}, debitDate: ${debitDate}")
          IO.pure(billing)
        case None =>
          IO.raiseError(
            new RuntimeException(
              s"Billing not found for member: ${member.id}, debitDate: $debitDate, email: ${inputRecord.email}"
            )
          )
      }
      nowInUTC <- DateTimeUtil.nowInUTC
    } yield RefundTransaction(
      id = UUID.randomUUID().toString,
      billingId = billing.id,
      bsb = inputRecord.bsb,
      accountName = inputRecord.name,
      accountNumber = String.valueOf(inputRecord.accountNumber),
      locationId = billing.locationId,
      memberId = member.id,
      refundAmount = inputRecord.amount,
      refundReason = inputRecord.reason,
      createdAt = nowInUTC,
      updatedAt = nowInUTC
    )
  }

  private def readInputRecords(): List[InputItem] =
    val source = Source.fromFile("input.json")
    val fileContent = source.mkString
    source.close()

    val inputRecords = parser.parse(fileContent).flatMap(_.as[List[InputItem]]) match
      case Right(records) => records
      case Left(error)    => throw new RuntimeException(s"Failed to parse input file: $error")

    inputRecords

  private def saveToFile(data: String): Unit =
    val file = Paths.get("output.json")
    Files.write(file, data.getBytes())

  def run(args: List[String]): IO[ExitCode] =
    val allRecords = readInputRecords()

    allRecords.traverse(generateRefundTransaction).flatMap { transactions =>
      saveToFile(transactions.asJson.spaces2)
      IO {
        ExitCode.Success
      }
    }
}
