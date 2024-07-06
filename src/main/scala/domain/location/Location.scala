package io.daniel
package domain.location

import cats.syntax.all._
import skunk.Codec
import skunk.codec.all.varchar

case class Location(id: String, name: String, email: Option[String])

object Location {
  val locationCodec: Codec[Location] =
    (varchar(50), varchar(100), varchar(50).opt).tupled.imap { case (id, name, email) =>
      Location(id, name, email)
    } { location =>
      (
        location.id,
        location.name,
        location.email
      )
    }
}
