package io.daniel
package db

import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.derivation.default._

import aws.SecretsManager

case class DbConfig(
    host: String,
    port: Int,
    username: String,
    dbname: String,
    password: String
) derives ConfigReader

object DbConfig:
  def load(env: String): Either[Error, DbConfig] =
    env match
      case "local" =>
        ConfigSource.default.at("db.local").load[DbConfig].left.map(f => Error(f.toList.mkString(",")))
      case _ =>
        val secretName = s"hub-insights-rds-cluster-readonly-$env"
        SecretsManager.getDbConfigFromSecret(secretName).left.map(e => Error(e.toString))
