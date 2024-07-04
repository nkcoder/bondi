## Introduction

A playground project using:

- [Scala 3](https://scala-lang.org/)
- [Skunk](https://github.com/typelevel/skunk): a functional PostgreSQL client for Scala.
- [PureConfig](https://github.com/pureconfig/pureconfig): a Scala library for loading configuration files.
- [PostgreSQL](https://www.postgresql.org/): a powerful, open source object-relational database system.

## Setup

Start the PostgreSQL database using Docker:

```shell
docker compose  up
```

Run the application:

```shell
auto/local io.daniel.Main
auto/dev io.daniel.apps.RefundApp
auto/prod io.daniel.apps.RefundApp
```

How to specify the application to run:

- Setting the mainClass in build.sbt
    ```sbt
    Compile / mainClass := Some("io.daniel.Main")
    Compile / mainClass := Some("io.daniel.apps.RefundApp")
    ```
- Using the `run` task with the `--main` option
    ```shell
    sbt "runRun io.daniel.Main"
    ```

## Trouble Shooting

### Secrets Manager can't find the specified secret.

> Secrets Manager can't find the specified secret. (Service: SecretsManager, Status Code: 400, Request ID: aab88d0e-ca3b-461a-8a6f-21371846fc63)


## References

- [The Skunk Scala Library for Database Interaction: A Comprehensive Guide](https://blog.rockthejvm.com/skunk-complete-guide/#8-sql-interpolation-query-and-command)
- [PureConfig doc](https://pureconfig.github.io/docs/)
- [Skunk doc](https://typelevel.org/skunk/)