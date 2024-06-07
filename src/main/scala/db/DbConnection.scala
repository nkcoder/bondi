package io.daniel
package db

import cats.effect.*
import cats.effect.std.Console
import fs2.io.net.Network
import natchez.Trace
import skunk.*

object DbConnection:
  def single[F[_]: Temporal: Trace: Network: Console](config: DbConfig): Resource[F, Session[F]] =
    Session.single(
      host = config.host,
      port = config.port,
      user = config.username,
      database = config.dbname,
      password = Some(config.password)
    )

  def pooled[F[_]: Temporal: Trace: Network: Console](config: DbConfig): Resource[F, Resource[F, Session[F]]] =
    Session.pooled(
      host = config.host,
      port = config.port,
      user = config.username,
      database = config.dbname,
      password = Some(config.password),
      max = 10
    )
