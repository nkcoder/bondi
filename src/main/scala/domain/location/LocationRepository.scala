package io.daniel
package domain.location

import db.Repository
import domain.location.Location.locationCodec

import cats.effect.Sync
import skunk.{Query, Session}
import skunk.codec.all.varchar
import skunk.implicits.sql

class LocationRepository[F[_]: Sync](session: Session[F]) extends Repository[F, Location](session) {
  import LocationRepository.*

  def findByName(name: String): F[Option[Location]] =
    findOneBy(selectByName, name)

}

object LocationRepository {
  def make[F[_]: Sync](session: Session[F]): F[LocationRepository[F]] =
    Sync[F].delay(new LocationRepository[F](session))

  private val selectByName: Query[String, Location] =
    sql"""
         SELECT id, name, email
         FROM location
         WHERE name = $varchar
       """.query(locationCodec)
}
