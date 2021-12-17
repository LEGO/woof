package org.legogroup.woof

import scala.scalajs.js.Date

val defaultTimeFormat: EpochMillis => String = e =>
  val date                                 = new Date(e.millis.toDouble)
  extension (l: Double) def prepad(n: Int) = l.toLong.toString.reverse.padTo(2, '0').reverse

  val month   = (date.getMonth() + 1).prepad(2)
  val day     = date.getDate().prepad(2)
  val yr      = date.getFullYear().prepad(4)
  val hour    = date.getHours().prepad(2)
  val minutes = date.getMinutes().prepad(2)
  val seconds = date.getSeconds().prepad(2)
  s"$yr-$month-$day $hour:$minutes:$seconds"
