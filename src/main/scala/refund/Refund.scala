package io.daniel
package refund

import skunk.codec.all.varchar
import skunk.{Decoder, ~}

case class Member(id: String)

object Member {
  val memberDecoder: Decoder[Member] = varchar(50).map(Member.apply)
}

case class Billing(id: String, locationId: String)
object Billing {
  val billingDecoder: Decoder[Billing] = (varchar(50) ~ varchar(50)).map { case id ~ locationId =>
    Billing(id, locationId)
  }
}

case class RefundTransaction(
    id: String,
    billingId: String,
    brandId: String = "6dec4e5f-7a07-4a7e-a809-2c0c1df01366",
    bsb: String,
    accountName: String,
    accountNumber: String,
    refundStatus: String = "SUCCESS",
    requestBy: String = "6b303448-4010-4ab8-a3dc-30bbd4145475",
    paymentType: String = "DIRECT_DEBIT",
    createdAt: String,
    locationId: String,
    memberId: String,
    refundAmount: BigDecimal,
    refundBy: String = "6b303448-4010-4ab8-a3dc-30bbd4145475",
    refundDate: String = "2024-05-30",
    refundReason: String,
    refundType: String = "BILLING",
    updatedAt: String
)

case class InputItem(
    name: String,
    email: String,
    ddDate: String,
    bsb: String,
    accountNumber: Long,
    reason: String,
    amount: BigDecimal,
    refundDate: String
)
