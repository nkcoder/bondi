> Bondi Beach: A world-famous beach in Sydney known for its golden sands, surf-friendly waves, vibrant beach culture,
> and the iconic Bondi Icebergs swimming club.

## Introduction

I have a deep passion for Scala and Functional Programming, although my professional work predominantly involves
JavaScript and TypeScript on Node.js.

To bridge this gap, I initiated this project to handle various one-time or recurring tasks. More importantly,
this project serves as a platform for me to learn and practice Scala 3, delve into Functional Programming with libraries
like Cats and ZIO, and explore other Scala libraries.

**Topics**:

- [Scala 3](https://scala-lang.org/)
- [Skunk](https://github.com/typelevel/skunk): a functional PostgreSQL client for Scala.
- [PureConfig](https://github.com/pureconfig/pureconfig): a Scala library for loading configuration files.
- [PostgreSQL](https://www.postgresql.org/): a powerful, open source object-relational database system.
- [cats](https://typelevel.org/cats/): a library that provides abstractions for functional programming in Scala.
- [cats-effect](https://typelevel.org/cats-effect/): a library that provides abstractions for effectful programming in
  Scala.
- [circe](https://circe.github.io/circe/): a JSON library for Scala.

## Apps

### RefundApp

**Run**:

```shell
auto/dev io.daniel.apps.RefundApp
auto/prod io.daniel.apps.RefundApp
```

### ClubTransfer

Read a CSV file, then send emails using AWS SES client.

How to run the application:

- changeAwsProfileToProd
- change the `paymentType` to `PIF` or `DD`
- put the corresponding csv file in the `src/main/resources` folder, file name should be `pif_club_transfer.json` or
  `dd_club_transfer.json`
- run the application: auto/prod io.daniel.apps.ClubTransfer
- can test by changing #185 to `toDaniel` email

**Run**:

```shell
auto/dev io.daniel.apps.ClubTransfer
auto/prod io.daniel.apps.ClubTransfer
```

### FixDuplicateEmails

Read a plain text file with emails, query and update Dynamodb data using AWS SDK.

```shell
auto/dev io.daniel.apps.FixDuplicateEmails
auto/prod io.daniel.apps.FixDuplicateEmails
```

## Setup

Start the local PostgreSQL database using Docker:

```shell
docker compose  up
```

## Trouble Shooting

### How to specify the application to run

- Setting the mainClass in `build.sbt`
    ```sbt
    Compile / mainClass := Some("io.daniel.Main")
    Compile / mainClass := Some("io.daniel.apps.RefundApp")
    ```
- Using the `run` task with the `--main` option
    ```shell
    sbt "runMain io.daniel.apps.FixDuplicateEmails"
    ```

### Secrets Manager can't find the specified secret.

> Secrets Manager can't find the specified secret. (Service: SecretsManager, Status Code: 400, Request ID:
> aab88d0e-ca3b-461a-8a6f-21371846fc63)

**Options**:

- Option 1: add environment variable `AWS_PROFILE=prod` to Intellij Idea Run/Debug configuration.
- Option 2: run on the command line: `AWS_PROFILE=prod auto/prod io.daniel.apps.ClubTransfer`

## References

- [The Skunk Scala Library for Database Interaction: A Comprehensive Guide](https://blog.rockthejvm.com/skunk-complete-guide/#8-sql-interpolation-query-and-command)
- [PureConfig doc](https://pureconfig.github.io/docs/)
- [Skunk doc](https://typelevel.org/skunk/)
- [Cats doc](https://typelevel.org/cats/)
- [Cats Effect doc](https://typelevel.org/cats-effect/)