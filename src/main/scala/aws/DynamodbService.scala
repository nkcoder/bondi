package io.daniel
package aws

import cats.effect.IO
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._

object DynamodbService {

  def getAsyncClient: IO[DynamoDbAsyncClient] = IO.delay {
    DynamoDbAsyncClient
      .builder()
      .region(Region.AP_SOUTHEAST_2)
      .build();
  }

  def updateItem(
      dynamodbClient: DynamoDbAsyncClient,
      tableName: String,
      key: String,
      keyVal: String,
      name: String,
      updateValue: String
  ): IO[UpdateItemResponse] = {
    import scala.jdk.CollectionConverters.*

    for {
      updateRequest <- IO.delay {
        val attributeValueForKey = AttributeValue.builder.s(keyVal).build;
        val itemKey              = Map(key -> attributeValueForKey)

        val attributeValueForUpdate = AttributeValueUpdate.builder
          .value(AttributeValue.builder.s(updateValue).build)
          .action(AttributeAction.PUT)
          .build()
        val updatedValues = Map(name -> attributeValueForUpdate)

        UpdateItemRequest.builder
          .tableName(tableName)
          .key(itemKey.asJava)
          .attributeUpdates(updatedValues.asJava)
          .build()
      }
      updateItemResponse <- IO.fromCompletableFuture(IO(dynamodbClient.updateItem(updateRequest)))
    } yield (updateItemResponse)
  }

  def queryItemsByIndex(
      dynamodbClient: DynamoDbAsyncClient,
      tableName: String,
      indexName: String,
      key: String,
      keyVal: String
  ): IO[List[Map[String, AttributeValue]]] = {
    import scala.jdk.CollectionConverters.*

    for {
      queryRequest <- IO.delay {
        val expressionAttributeNames  = Map("#key" -> key)
        val expressionAttributeValues = Map(":keyVal" -> AttributeValue.builder.s(keyVal).build)
        QueryRequest.builder
          .tableName(tableName)
          .indexName(indexName)
          .keyConditionExpression("#key = :keyVal")
          .expressionAttributeNames(expressionAttributeNames.asJava)
          .expressionAttributeValues(expressionAttributeValues.asJava)
          .build
      }
      queryResponse <- IO.fromCompletableFuture(IO(dynamodbClient.query(queryRequest)))
      items         <- IO.delay(queryResponse.items().asScala.toList.map(_.asScala.toMap))
    } yield items
  }
}
