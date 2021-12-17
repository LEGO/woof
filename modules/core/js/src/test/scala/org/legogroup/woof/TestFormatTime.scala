package org.legogroup.woof

import scala.scalajs.js.Date

val testFormatTime = (e: EpochMillis) =>
  val twoHours = 1000 * 60 * 60 * 2 // copenhagen offset
  val date     = new Date(e.millis.toDouble + twoHours)
  date.toISOString().replace("T", " ").take(19)
