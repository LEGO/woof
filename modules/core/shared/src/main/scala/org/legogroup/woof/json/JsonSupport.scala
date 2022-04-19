package org.legogroup.woof.json

import org.legogroup.woof.{EpochMillis, LogLine}

class JsonSupport:

  def toJsonString(logLine: LogLine, epochMillis: EpochMillis): String =
    val contextPart = (if logLine.context.nonEmpty then "," else "") + logLine.context.map((key,value) => s""""${JsonSupport.escape(key)}":"${JsonSupport.escape(value)}"""").mkString(",")
    val formattedTime = org.legogroup.woof.defaultTimeFormat(epochMillis)
    s"""{"level":"${logLine.level}","epochMillis":"${epochMillis.millis}","timeStamp":"$formattedTime","enclosingClass":"${logLine.info.enclosingClass.fullName}","message":"${JsonSupport.escape(logLine.message)}"$contextPart}"""

end JsonSupport

object JsonSupport:

  val escapeMap = Map(
    '\b' -> "\\b",
    '\f' -> "\\f",
    '\n' -> "\\n",
    '\r' -> "\\r",
    '\t' -> "\\t",
    '"' -> "\\\"",
    '\\' -> "\\"
  )

  def escape(str: String): String =
    str
      .flatMap(c => escapeMap.getOrElse(c, c.toString))
      .filterNot(_.isControl)

end JsonSupport


