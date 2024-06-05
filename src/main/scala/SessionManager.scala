package io.daniel

import cats.effect.*
import com.typesafe.config.{Config, ConfigFactory}
import natchez.Trace.Implicits.noop
import skunk.*

object SessionManager:

  private def getConfig: Config =
    val config = ConfigFactory.load()
    config

  def get: Resource[IO, Session[IO]] =
    val config = getConfig
    Session.single(
      host = config.getString("db.host"),
      port = config.getInt("db.port"),
      user = config.getString("db.user"),
      database = config.getString("db.database"),
      password = Some(config.getString("db.password"))
    )
