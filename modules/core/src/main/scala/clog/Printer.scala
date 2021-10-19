package clog

import java.time.Instant
import Logger.LogLevel

trait Printer:
  def toPrint(
      instant: Instant,
      level: LogLevel,
      info: Logging.LogInfo,
      message: String,
      context: List[(String, String)],
  ): String
end Printer
