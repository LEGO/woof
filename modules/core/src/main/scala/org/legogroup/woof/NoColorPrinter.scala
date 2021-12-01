package org.legogroup.woof

import org.legogroup.woof.ColorPrinter.Theme

import java.time.{Instant, ZoneId}
import scala.util.chaining.scalaUtilChainingOps

class NoColorPrinter(formatTime: Instant => String = ColorPrinter.defaultDateTimeFormat)
    extends ColorPrinter(theme = NoColorPrinter.noColorTheme, formatTime = formatTime):
end NoColorPrinter

object NoColorPrinter:
  val noColorTheme = ColorPrinter.Theme(_ => Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty)
end NoColorPrinter
