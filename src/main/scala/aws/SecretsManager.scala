package io.daniel
package aws
import db.DbConfig

import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import io.circe.{Decoder, Error}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

object SecretsManager:
  implicit val dbConfigDecoder: Decoder[DbConfig] = deriveDecoder[DbConfig]

  private def getSecret(secretName: String): String =
    println(s"Getting secret: $secretName")
    val client   = SecretsManagerClient.builder().region(Region.AP_SOUTHEAST_2).build()
    val request  = GetSecretValueRequest.builder().secretId(secretName).build()
    val response = client.getSecretValue(request)
    response.secretString()

  def getDbConfigFromSecret(secretName: String): Either[Error, DbConfig] =
    decode(getSecret(secretName))
