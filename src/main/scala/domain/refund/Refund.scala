package io.daniel
package refund

import skunk.codec.all.varchar
import skunk.{Decoder, ~}

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
