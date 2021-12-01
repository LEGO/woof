package org.legogroup.woof

import org.legogroup.woof.Logger.LogLevel

import java.time.Instant

trait Printer:
  def toPrint(
      instant: Instant,
      level: LogLevel,
      info: Logging.LogInfo,
      message: String,
      context: List[(String, String)],
  ): String
end Printer
