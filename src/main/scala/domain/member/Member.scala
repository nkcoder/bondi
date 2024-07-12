package io.daniel
package domain.member

import cats.syntax.all._
import skunk.Codec
import skunk.codec.all.int4
import skunk.codec.all.numeric
import skunk.codec.all.varchar

case class Member(
    id: String,
    mobileNumber: String,
    email: String,
    outstandingBalance: Option[BigDecimal],
    ddFailureCount: Option[Int]
)

object Member {

  /** Encoder and decoder: Postgres column type to Scala type and vice versa <p>
    *   1. if the field is nullable in the database, use .opt <p> 2. the length of the varchar should be the same as the
    *   column in the database <p> 3. the precision and scale of the numeric should be the same as the column in the
    *   database
    */
  val memberCodec: Codec[Member] =
    (varchar(50), varchar(512), varchar(512), numeric(27, 5).opt, int4.opt).tupled.imap {
      case (id, mobileNumber, email, outstandingBalance, ddFailureCount) =>
        Member(id, mobileNumber, email, outstandingBalance, ddFailureCount)
    } { member =>
      (
        member.id,
        member.mobileNumber,
        member.email,
        member.outstandingBalance,
        member.ddFailureCount
      )
    }
}
