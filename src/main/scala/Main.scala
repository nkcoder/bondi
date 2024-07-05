package io.daniel

import db.{DbConfig, DbConnection}
import domain.member.{Member, MemberRepository}

import cats.effect.{ExitCode, IO, IOApp}
import natchez.Trace.Implicits.noop
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

import scala.util.Properties

object Main extends IOApp {
  // local testing
  private def singleSession(config: DbConfig): IO[Unit] =
    DbConnection.single[IO](config).use { session =>
      for {
        _           <- IO(println("Using a single session"))
        dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
        _           <- IO(println(s"Current date and time: $dateAndTime"))
      } yield ()
    }

  private def pooledSession(config: DbConfig): IO[Unit] =
    DbConnection.pooled[IO](config).use { resource =>
      resource.use { session =>
        for {
          _           <- IO(println("Using a pooled session"))
          dateAndTime <- session.unique(sql"select current_timestamp".query(timestamptz))
          _           <- IO(println(s"Current date and time: $dateAndTime"))
        } yield ()
      }
    }

  override def run(args: List[String]): IO[ExitCode] =
    val env = Properties.envOrElse("APP_ENV", "local")
    DbConfig.load(env).fold(
      error => IO(println(error.toString)).as(ExitCode.Error),
      config =>
        runSessions(config)
        runOnMember(config)
    )

  private def runSessions(dbConfig: DbConfig): IO[ExitCode] =
    singleSession(dbConfig) *> pooledSession(dbConfig) *> IO.pure(ExitCode.Success)

  private def runOnMember(dbConfig: DbConfig): IO[ExitCode] =
    DbConnection.pooled[IO](dbConfig).use { resource =>
      resource.use { session =>
        {
          for {
            memberRepo <- MemberRepository.make[IO](session)
            _          <- IO(println("Creating users" + "_" * 50))
            johnId     <- memberRepo.create("john-1", "email@john.com", 0.5, 1)
            _          <- IO(println(s"John created with id: $johnId"))
            jacobId    <- memberRepo.create("jacob-1", "email@jacob.com", 1, 1)
            _          <- IO(println(s"Jacob created with id: $jacobId"))
            kendrickId <- memberRepo.create("kendrick-1", "email@kendrick.com", 2, 3)
            _          <- IO(println(s"Kendrick created with id: $kendrickId"))
            _          <- IO(println("Fetching all users" + "_" * 50))
            users_1    <- memberRepo.findAll.compile.toList
            _          <- IO(println(s"Users found: $users_1"))
            _          <- IO(println("Update John's email to: email@email.com" + "_" * 50))
            _          <- memberRepo.update(Member(johnId, "John-2", "email@email.com", Some(0.6), Some(2)))
            _          <- IO(println("Fetching all users" + "_" * 50))
            users_2    <- memberRepo.findAll.compile.toList
            _          <- IO(println(s"Users found: $users_2"))
            _          <- IO(println("Deleting John" + "_" * 50))
            _          <- memberRepo.delete(johnId)
            _          <- IO(println("Fetching all users" + "_" * 50))
            users_3    <- memberRepo.findAll.compile.toList
            _          <- IO(println(s"Users found: $users_3"))
          } yield ExitCode.Success
        }
      }
    }

}
