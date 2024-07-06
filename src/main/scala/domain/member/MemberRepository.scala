package io.daniel
package domain.member

import java.util.UUID

import cats.effect.Sync
import cats.syntax.all._
import fs2.Stream
import skunk.Command
import skunk.Query
import skunk.Session
import skunk.Void
import skunk.codec.all._
import skunk.syntax.all._

import db.Repository
import domain.member.Member.memberCodec

final class MemberRepository[F[_]: Sync](session: Session[F]) extends Repository[F, Member](session) {

  import MemberRepository.*

  def create(mobileNumber: String, email: String, outstandingBalance: BigDecimal, ddFailureCount: Int): F[String] =
    for {
      cmd <- session.prepare(insert)
      memberId = UUID.randomUUID().toString
      _ <- cmd.execute(Member(memberId, mobileNumber, email, Option(outstandingBalance), Option(ddFailureCount)))
    } yield memberId

  def findAll: Stream[F, Member] =
    Stream.evalSeq(session.execute(selectAll))

  def findById(id: String): F[Option[Member]] =
    findOneBy(selectById, id)

  def update(member: Member): F[Unit] =
    update(_update, member)

  def delete(id: String): F[Unit] =
    update(_delete, id)

  def findByEmail(email: String): F[Option[Member]] =
    findOneBy(selectByEmail, email)

}

object MemberRepository {
  def make[F[_]: Sync](session: Session[F]): F[MemberRepository[F]] =
    Sync[F].delay(new MemberRepository[F](session))

  private val selectAll: Query[Void, Member] =
    sql"""
         SELECT id, mobile_number, email, outstanding_balance, dd_failure_count
         FROM member
       """.query(memberCodec)

  private val selectById: Query[String, Member] =
    sql"""
         SELECT id, mobile_number, email, outstanding_balance, dd_failure_count
         FROM member
         WHERE id = $varchar
       """.query(memberCodec)

  private val selectByEmail: Query[String, Member] =
    sql"""
         SELECT id, mobile_number, email, outstanding_balance, dd_failure_count
         FROM member
         WHERE email = $varchar
       """.query(memberCodec)

  private val insert: Command[Member] =
    sql"""
         INSERT INTO member (id, mobile_number, email, outstanding_balance, dd_failure_count)
         VALUES ($memberCodec)
       """.command

  private val _update: Command[Member] =
    sql"""
         UPDATE member
         SET mobile_number = $varchar, email = $varchar, outstanding_balance = $numeric, dd_failure_count = $int4
         WHERE id = $varchar
       """.command.contramap { (member: Member) =>
      (member.mobileNumber, member.email, member.outstandingBalance.getOrElse(BigDecimal(0)), member.ddFailureCount.getOrElse(0), member.id)
    }

  private val _delete: Command[String] =
    sql"""
         DELETE FROM member
         WHERE id = $varchar
       """.command.contramap(identity)

}
