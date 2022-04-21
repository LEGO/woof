package org.legogroup.woof

import org.legogroup.woof.json.JsonSupport

class JsonPrinter extends Printer:

  def toPrint(
      epochMillis: EpochMillis,
      level: LogLevel,
      info: LogInfo,
      message: String,
      context: List[(String, String)],
  ): String =
    JsonSupport.toJsonString(LogLine(level, info, message, context), epochMillis)

end JsonPrinter
