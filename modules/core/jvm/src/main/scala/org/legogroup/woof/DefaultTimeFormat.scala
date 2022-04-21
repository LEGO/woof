package org.legogroup.woof

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}

val defaultTimeFormat: EpochMillis => String = e =>
  DateTimeFormatter
    .ofPattern("YYYY-MM-dd HH:mm:ss")
    .withZone(ZoneId.systemDefault())
    .format(Instant.ofEpochMilli(e.millis))

val isoTimeFormat: EpochMillis => String = e => 
  val split = Instant.ofEpochMilli(e.millis).toString.split("\\.") 
  if split.length == 1 then split(0) else split(0) + "Z"

