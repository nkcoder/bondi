package io.daniel
package support

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import cats.effect.IO

object DateTimeUtil:
  def nowInUTC: IO[String] = IO {
    val utcNow: Instant = Instant.now()
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC)
    val formattedUtcNow: String = formatter.format(utcNow)
    formattedUtcNow
  }

  def formatDate(inputDate: String): String =
    val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val date = LocalDate.parse(inputDate, inputFormatter)
    date.format(outputFormatter)
