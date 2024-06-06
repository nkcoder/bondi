package io.daniel
package domain.refund

case class Refund(
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
