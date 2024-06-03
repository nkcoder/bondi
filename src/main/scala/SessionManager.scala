package io.daniel

import cats.effect.*
import natchez.Trace.Implicits.noop
import skunk.*


object SessionManager:
  def dev: Resource[IO, Session[IO]] =
    Session.single(
      host = "hub-insights-cluster-dev.cluster-ro-cww3ddulf0f5.ap-southeast-2.rds.amazonaws.com",
      port = 5432,
      user = "hub_insights_rds_cluster_readonly",
      database = "hub_insights",
      password = Some("gAuudx*ZL-exg-Jj3gzf")
    )

  def prod: Resource[IO, Session[IO]] =
    Session.single(
      host = "hub-insights-cluster-prod.cluster-ro-cnfhlchb9rtt.ap-southeast-2.rds.amazonaws.com",
      port = 5432,
      user = "hub_insights_rds_cluster_prod_readonly",
      database = "hub_insights",
      password = Some("htZ3N@*CUj7*xq_8@msc")
    )