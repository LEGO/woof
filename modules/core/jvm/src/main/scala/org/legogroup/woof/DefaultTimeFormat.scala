package org.legogroup.woof

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

val defaultTimeFormat: EpochMillis => String = e =>
  DateTimeFormatter
    .ofPattern("YYYY-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())
    .format(Instant.ofEpochMilli(e.millis))

val isoTimeFormat: EpochMillis => String = e => Instant.ofEpochMilli(e.millis).toString