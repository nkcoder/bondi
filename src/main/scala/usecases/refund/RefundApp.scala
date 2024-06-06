package io.daniel
package usecases.refund

//import cats.effect.*
//import cats.implicits.*
//import io.circe.generic.auto.*
//import io.circe.parser
//import io.circe.syntax.*
//import io.daniel.db.{DbConfig, DbConnection}
//import io.daniel.domain.Billing.billingDecoder
//import io.daniel.domain.member.Member.memberDecoder
//import io.daniel.domain.{Billing, Member, RefundTransaction}
//import skunk.*
//import skunk.codec.all.*
//import skunk.implicits.*
//
//import java.nio.file.{Files, Paths}
//import java.util.UUID
//import scala.io.Source

case class InputItem(
    name: String,
    email: String,
    ddDate: String,
    bsb: String,
    accountNumber: Long,
    reason: String,
    amount: BigDecimal,
    refundDate: String
)

object RefundApp
//object RefundApp extends IOApp:

//  private val session: Resource[IO, Session[IO]] = DbConnection.single(config = DbConfig("localhost", 5432, "postgres", "postgres", "test"))
//
//  private val getMember: Query[String, Member] = sql"SELECT id FROM member WHERE email = $varchar".query(memberDecoder)
//  private val getBilling: Query[String *: String *: EmptyTuple, Billing] =
//    sql"SELECT id, location_id FROM member_contract_billing mcb WHERE member_id = $varchar AND debit_date = $varchar::timestamp with time zone"
//      .query(billingDecoder)
//
//  private def generateRefundTransaction(inputRecord: InputItem): IO[Option[RefundTransaction]] = session.use { s =>
//    for {
//      getMemberQuery <- s.prepare(getMember)
//      memberOption <- getMemberQuery.option(inputRecord.email)
//      getBillingQuery <- s.prepare(getBilling)
//      debitDate = if inputRecord.ddDate.isEmpty then inputRecord.refundDate else inputRecord.ddDate
//      billingOption <- memberOption match {
//        case Some(member) =>
//          println(s"Member found for email: ${inputRecord.email}")
//          getBillingQuery.option(member.id, debitDate)
//        case None =>
//          println(s"[WARN] Member not found for email: ${inputRecord.email}")
//          IO.pure(None)
//      }
//      result <- (memberOption, billingOption) match
//        case (Some(member), Some(billing)) =>
//          println(s"Billing found for member: ${member.id}, debitDate: $debitDate")
//          for {
//            nowInUTC <- DateTimeUtil.nowInUTC
//          } yield Some(
//            RefundTransaction(
//              id = UUID.randomUUID().toString,
//              billingId = billing.id,
//              bsb = inputRecord.bsb,
//              accountName = inputRecord.name,
//              accountNumber = String.valueOf(inputRecord.accountNumber),
//              locationId = billing.locationId,
//              memberId = member.id,
//              refundAmount = inputRecord.amount,
//              refundReason = "MANUAL" + "_" + inputRecord.reason,
//              createdAt = nowInUTC,
//              updatedAt = nowInUTC
//            )
//          )
//        case (Some(member), None) =>
//          println(
//            s"--- [WARN] No billing found for member: ${member.id}, email: ${inputRecord.email}, debitDate: $debitDate"
//          )
//          IO.pure(None)
//        case _ => IO.pure(None)
//    } yield result
//  }
//
//  private def readInputRecords: IO[List[InputItem]] =
//    IO {
//      val source = Source.fromFile("input.json")
//      val fileContent = source.mkString
//      source.close()
//
//      val inputRecords = parser.parse(fileContent).flatMap(_.as[List[InputItem]]) match
//        case Right(records) => records
//        case Left(error)    => throw new RuntimeException(s"Failed to parse input file: $error")
//
//      inputRecords
//    }
//
//  private def saveToFile(data: String): IO[Unit] =
//    IO {
//      val file = Paths.get("output.json")
//      Files.write(file, data.getBytes())
//    }
//
//  def run(args: List[String]): IO[ExitCode] =
//    for {
//      allRecords <- readInputRecords
//      refundTransactions <- allRecords.traverse(generateRefundTransaction)
//      _ <- saveToFile(refundTransactions.flatten.asJson.spaces2)
//      _ = println("Output file generated successfully!")
//    } yield ExitCode.Success
