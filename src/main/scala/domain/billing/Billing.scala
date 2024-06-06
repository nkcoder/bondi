package io.daniel
package domain.billing

import cats.syntax.all.*
import skunk.*
import skunk.codec.all.{date, numeric, varchar}

import java.time.LocalDate

case class Billing(
    id: String,
    locationId: String,
    memberId: String,
    contractId: String,
    debitDate: LocalDate,
    debitAmount: BigDecimal,
    paymentType: String
)

object Billing {

  /** encoder and decoder: Postgres column type to Scala type and vice versa
    */
  val billingCodec: Codec[Billing] =
    (varchar(50), varchar(50), varchar(50), varchar(50), date, numeric(14, 5), varchar(50)).tupled.imap {
      case (id, locationId, memberId, contractId, debitDate, debitAmount, paymentType) =>
        Billing(id, locationId, memberId, contractId, debitDate, debitAmount, paymentType)
    } { billing =>
      (
        billing.id,
        billing.locationId,
        billing.memberId,
        billing.contractId,
        billing.debitDate,
        billing.debitAmount,
        billing.paymentType
      )
    }
}
