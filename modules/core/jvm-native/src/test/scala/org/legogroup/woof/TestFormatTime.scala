package org.legogroup.woof

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.TimeZone

val testFormatTime = (e: EpochMillis) =>
  DateTimeFormatter
    .ofPattern("YYYY-MM-dd HH:mm:ss")
    .withZone(ZoneId.of("Europe/Copenhagen"))
    .format(Instant.ofEpochMilli(e.millis))
