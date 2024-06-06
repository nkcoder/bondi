package io.daniel
package domain.member

import db.Repository
import domain.member.Member

import cats.effect.Sync
import cats.syntax.all.*
import fs2.Stream
import skunk.codec.all.*
import skunk.syntax.all.*
import skunk.{Codec, Command, Query, Session, Void}

import java.util.UUID

final class MemberRepository[F[_]: Sync](session: Session[F]) extends Repository[F, Member](session) {

  import MemberRepository.*

  def create(mobileNumber: String, email: String, outstandingBalance: BigDecimal, ddFailureCount: Int): F[String] =
    for {
      cmd <- session.prepare(insert)
      memberId = UUID.randomUUID().toString
      _ <- cmd.execute(Member(memberId, mobileNumber, email, outstandingBalance, ddFailureCount))
    } yield memberId

  def findAll: Stream[F, Member] =
    Stream.evalSeq(session.execute(selectAll))

  def findById(id: String): F[Option[Member]] =
    findOneBy(selectById, id)

  def update(member: Member): F[Unit] =
    update(_update, member)

  def delete(id: String): F[Unit] =
    update(_delete, id)

}

object MemberRepository {
  def make[F[_]: Sync](session: Session[F]): F[MemberRepository[F]] =
    Sync[F].delay(new MemberRepository[F](session))

  private val codec: Codec[Member] =
    (varchar(50), varchar(512), varchar(50), numeric(14, 5), int4).tupled.imap {
      case (id, mobileNumber, email, outstandingBalance, ddFailureCount) =>
        Member(id, mobileNumber, email, outstandingBalance, ddFailureCount)
    } { member => (member.id, member.mobileNumber, member.email, member.outstandingBalance, member.ddFailureCount) }

  private val selectAll: Query[Void, Member] =
    sql"""
         SELECT id, mobile_number, email, outstanding_balance, dd_failure_count
         FROM member
       """.query(codec)

  private val selectById: Query[String, Member] =
    sql"""
         SELECT id, mobile_number, email, outstanding_balance, dd_failure_count
         FROM member
         WHERE id = $varchar
       """.query(codec)

  private val insert: Command[Member] =
    sql"""
         INSERT INTO member (id, mobile_number, email, outstanding_balance, dd_failure_count)
         VALUES ($codec)
       """.command

  private val _update: Command[Member] =
    sql"""
         UPDATE member
         SET mobile_number = $varchar, email = $varchar, outstanding_balance = $numeric, dd_failure_count = $int4
         WHERE id = $varchar
       """.command.contramap { member =>
      (member.mobileNumber, member.email, member.outstandingBalance, member.ddFailureCount, member.id)
    }

  private val _delete: Command[String] =
    sql"""
         DELETE FROM member
         WHERE id = $varchar
       """.command.contramap(identity)

}
