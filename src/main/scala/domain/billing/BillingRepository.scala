package io.daniel
package domain.billing

import cats.effect.Sync
import cats.syntax.all._
import fs2.Stream
import skunk.Query
import skunk.Session
import skunk.Void
import skunk.codec.all._
import skunk.syntax.all._

import db.Repository

final class BillingRepository[F[_]: Sync](session: Session[F]) extends Repository[F, Billing](session) {
  import BillingRepository.*

  def findAll: Stream[F, Billing] = Stream.evalSeq(session.execute(selectAll))

  def findById(id: String): F[Option[Billing]] = findOneBy(selectById, id)

  def findByMemberIdAndDebitDate(memberId: String, debitDate: String): F[Option[Billing]] =
    findOneBy(selectByMemberIdAndDebitDate, (memberId, debitDate))

}

object BillingRepository {
  def make[F[_]: Sync](session: Session[F]): F[BillingRepository[F]] =
    Sync[F].delay(new BillingRepository(session))

  private val selectAll: Query[Void, Billing] =
    sql"""
         SELECT id, location_id, member_id, member_contract_id as contract_id, debit_date, debit_amount, payment_type
         FROM member_contract_billing
       """.query(Billing.billingCodec)

  private val selectById: Query[String, Billing] =
    sql"""
         SELECT id, location_id, member_id,  member_contract_id as contract_id, debit_date, debit_amount, payment_type
         FROM member_contract_billing
         WHERE id = $varchar
       """.query(Billing.billingCodec)

  private val selectByMemberIdAndDebitDate: Query[(String, String), Billing] =
    sql"""
         SELECT id, location_id, member_id,  member_contract_id as contract_id, debit_date::date, debit_amount, payment_type
         FROM member_contract_billing
         WHERE member_id = $varchar AND debit_date = $varchar::timestamp with time zone
       """.query(Billing.billingCodec)

}
