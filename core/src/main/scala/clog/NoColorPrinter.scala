package clog

import java.time.Instant
import scala.util.chaining.scalaUtilChainingOps
import Logger.LogLevel
import java.time.ZoneId

class NoColorPrinter(zoneId: ZoneId = ZoneId.systemDefault()) extends ColorPrinter(zoneId = zoneId):
  override def toPrint(instant: Instant, level: LogLevel, info: Logging.LogInfo, message: String) =
    super.toPrint(instant, level, info, message).pipe(NoColorPrinter.removeColorCodes)
end NoColorPrinter

object NoColorPrinter:
  def removeColorCodes(str: String) = str.replaceAll("\u001B\\[[;\\d]*m", "");
end NoColorPrinter
