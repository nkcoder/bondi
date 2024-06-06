package io.daniel
package db

import cats.effect.Sync
import cats.syntax.all.*
import skunk.{Command, Query, Session}

/** A: the argument type
  *
  * @tparam F
  *   the effect type
  * @tparam E
  *   the domain to be used
  */
trait Repository[F[_], E](session: Session[F]) {
  protected def findOneBy[A](query: Query[A, E], argument: A)(using F: Sync[F]): F[Option[E]] =
    for {
      preparedQuery <- session.prepare(query)
      result        <- preparedQuery.option(argument)
    } yield result

  protected def update[A](command: Command[A], argument: A)(using F: Sync[F]): F[Unit] =
    for {
      preparedCommand <- session.prepare(command)
      _               <- preparedCommand.execute(argument)
    } yield ()
}
