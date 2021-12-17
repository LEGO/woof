package org.legogroup.woof

import java.time.Instant
import scala.concurrent.duration.FiniteDuration

trait Printer:
  def toPrint(
      epochMillis: EpochMillis,
      level: LogLevel,
      info: LogInfo,
      message: String,
      context: List[(String, String)],
  ): String
end Printer
