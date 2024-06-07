package io.daniel
package db

import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.derivation.default.*
import pureconfig.{ConfigReader, ConfigSource}

import scala.util.Properties

case class DbConfig(
    host: String,
    port: Int,
    user: String,
    database: String,
    password: String
) derives ConfigReader

object DbConfig:
  private val dbEnv = "db." + Properties.envOrElse("APP_ENV", "dev")

  def load: Either[ConfigReaderFailures, DbConfig] =
    ConfigSource.default.at(dbEnv).load[DbConfig]
