package clog

import java.time.Instant
import scala.util.chaining.scalaUtilChainingOps
import Logger.LogLevel
import java.time.ZoneId
import ColorPrinter.Theme

class NoColorPrinter(zoneId: ZoneId = ZoneId.systemDefault())
    extends ColorPrinter(theme = NoColorPrinter.noColorTheme, zoneId = zoneId):
end NoColorPrinter

object NoColorPrinter:
  val noColorTheme = ColorPrinter.Theme(_ => Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty)
end NoColorPrinter
