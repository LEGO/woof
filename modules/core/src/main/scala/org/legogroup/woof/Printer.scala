package org.legogroup.woof

import java.time.Instant

trait Printer:
  def toPrint(
      instant: Instant,
      level: LogLevel,
      info: LogInfo,
      message: String,
      context: List[(String, String)],
  ): String
end Printer
