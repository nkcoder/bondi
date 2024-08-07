package io.daniel
package apps

import scala.util.Properties
import scala.util.Using

import cats.effect.IO
import cats.effect.IOApp
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse

import aws.DynamodbService

/** <p> Find all unaccepted members with duplicate email, add suffix to the emails. Input: duplicate_emails.txt </p>
  *
  * <p> select distinct email from member where (accepted is null or accepted = false) group by email having count(1) >
  * 1; </p
  */
object FixDuplicateEmails extends IOApp.Simple {

  override def run: IO[Unit] = {
    val env = Properties.envOrElse("APP_ENV", "local")
    println(s"Running on $env environment")
    val tableSuffix =
      Option.when(env == "prod")("k5r5hyaijzgszl2xs5ysna5urq-prod").getOrElse("jyq7cvj6s5cw3dpsd6pvprmfbu-amritnew")
    val memberTable = s"Member-$tableSuffix"

    println(s"It is running on $env environment, table name: $memberTable")

    import cats.syntax.all.*

    for {
      dynamodbClient  <- DynamodbService.getAsyncClient
      duplicateEmails <- readEmailsFromFile("duplicate_emails.txt")
      _               <- IO.println(s"Found ${duplicateEmails.size} duplicate emails")
      - <- duplicateEmails.traverseWithIndexM((email, index) =>
        processOneEmail(dynamodbClient, memberTable, email, index)
      )
      _ <- IO.delay(dynamodbClient.close())
    } yield ()
  }

  private def processOneEmail(
      dynamoDBClient: DynamoDbAsyncClient,
      memberTable: String,
      email: String,
      index: Int
  ): IO[Unit] = {
    for {
      _ <- IO.println(s"Start to process the #${index + 1} item with email: $email")
      items <- DynamodbService.queryItemsByIndex(
        dynamoDBClient,
        tableName = memberTable,
        indexName = "memberByEmail",
        key = "email",
        keyVal = email
      )
      _ <- IO.println(s"Found ${items.size} items with email: $email")
      _ <- updateMemberEmail(dynamoDBClient, memberTable, items)
      _ <- IO.println(s"Complete process for email: $email")
    } yield ()
  }

  private def readEmailsFromFile(filePath: String): IO[List[String]] = {
    import scala.io.Source
    IO.fromTry {
      Using(Source.fromFile(filePath)) { source =>
        source.getLines().toList
      }
    }
  }

  private def updateMemberEmail(
      dynamodbClient: DynamoDbAsyncClient,
      tableName: String,
      items: List[Map[String, AttributeValue]]
  ): IO[List[UpdateItemResponse]] = {
    import cats.syntax.all.*

    items.filter(_.size > 1).traverseWithIndexM { (item, index) =>
      val memberId = item("memberId").s
      IO.println(s"Processing the ${index + 1} item, memberId: $memberId") *>
        IO.delay {
          val email            = item("email").s
          val (prefix, suffix) = email.splitAt(email.lastIndexOf("@"))
          val updatedEmail     = prefix + "_MANUAL_" + (index + 1) + suffix
          DynamodbService.updateItem(dynamodbClient, tableName, "memberId", memberId, "email", updatedEmail)
        }.flatten
    }
  }
}
