package org.legogroup.woof.json

import org.legogroup.woof.{EpochMillis, LogLine}
import org.legogroup.woof.json.JsonSupport.Json.JsonString
import java.nio.charset.StandardCharsets

object JsonSupport:

  private[json] def toJson(logLine: LogLine, epochMillis: EpochMillis): Json =
    val context       = logLine.context.map((key, value) => key -> Json.fromString(value))
    val contextJson   = Json.fromMap(context)
    val formattedTime = org.legogroup.woof.isoTimeFormat(epochMillis)
    Json.fromMap(
      List(
        "level"          -> Json.fromString(logLine.level.toString),
        "epochMillis"    -> Json.fromLong(epochMillis.millis),
        "timeStamp"      -> Json.fromString(formattedTime),
        "enclosingClass" -> Json.fromString(logLine.info.enclosingClass.fullName),
        "message"        -> Json.fromString(logLine.message),
        "context"        -> contextJson
      )
    )
  end toJson

  def toJsonString(logLine: LogLine, epochMillis: EpochMillis): String =
    Json.toStringNoSpaces(toJson(logLine, epochMillis))

  private[json] enum Json:
    case JsonObject(kvps: List[(JsonString, Json)])
    case JsonString(str: String)
    case JsonNumber(long: Long)
    case JsonBool(bool: Boolean)

  private[json] object Json:

    def fromLong(l: Long): Json.JsonNumber     = Json.JsonNumber(l)
    def fromString(s: String): Json.JsonString = JsonString(s)
    def fromMap(kvps: List[(String, Json)]): Json =
      Json.JsonObject(kvps.toList.map((key, value) => fromString(key) -> value))

    def toStringNoSpaces(json: Json): String =
      json match
        case Json.JsonObject(kvps) =>
          "{" + kvps.toList
            .map((key, value) => s"""${toStringNoSpaces(key)}:${toStringNoSpaces(value)}""")
            .mkString(",") + "}"
        case Json.JsonString(str)  => s"\"${escape(str)}\""
        case Json.JsonNumber(long) => long.toString
        case Json.JsonBool(bool)   => bool.toString

  end Json

  /** Credit to Circe for this
    * https://github.com/circe/circe/blob/3c4580219c4bbbb8ba6245fb7e4bce548bd07121/modules/core/shared/src/main/scala/io/circe/Printer.scala#L295
    */
  private[json] def toHex(nibble: Int): Char = (nibble + (if nibble >= 10 then 87 else 48)).toChar

  private[json] def escapedCharToHex(c: Char): String =
    new String(
      Array('u', toHex((c >> 12) & 15), toHex((c >> 8) & 15), toHex((c >> 4) & 15), toHex(c & 15)),
    )

  private[json] def escapedChar(ch: Char): String = ch match
    case '\b' => "\\b"
    case '\t' => "\\t"
    case '\n' => "\\n"
    case '\f' => "\\f"
    case '\r' => "\\r"
    case '"'  => "\\\""
    case '\\' => "\\\\"
    case _ =>
      if ch.isControl then "\\" + escapedCharToHex(ch)
      else String.valueOf(ch)

  private[json] def escape(str: String): String = str.flatMap(escapedChar)

end JsonSupport
