package io.daniel
package domain.member

case class Member(id: String, mobileNumber: String, email: String, outstandingBalance: BigDecimal, ddFailureCount: Int)
