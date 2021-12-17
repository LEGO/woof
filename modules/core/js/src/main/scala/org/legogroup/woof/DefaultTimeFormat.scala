package org.legogroup.woof

import scala.scalajs.js.Date

val defaultTimeFormat: EpochMillis => String = e =>
  val date = new Date(e.millis.toDouble)
  date.toISOString().replace("T", " ").take(19)
