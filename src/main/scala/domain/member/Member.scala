package io.daniel
package domain.member

import cats.syntax.all.*
import skunk.Codec
import skunk.codec.all.{int4, numeric, varchar}

case class Member(
    id: String,
    mobileNumber: String,
    email: String,
    outstandingBalance: BigDecimal,
    ddFailureCount: Int
)

object Member {

  /** encoder and decoder: Postgres column type to Scala type and vice versa
    */
  val memberCodec: Codec[Member] =
    (varchar(50), varchar(512), varchar(50), numeric(14, 5), int4).tupled.imap {
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
