package org.legogroup.woof

import java.time.{Instant, ZoneId}

import scala.util.chaining.scalaUtilChainingOps

import Logger.LogLevel
import ColorPrinter.Theme

class NoColorPrinter(formatTime: Instant => String = ColorPrinter.defaultDateTimeFormat)
    extends ColorPrinter(theme = NoColorPrinter.noColorTheme, formatTime = formatTime):
end NoColorPrinter

object NoColorPrinter:
  val noColorTheme = ColorPrinter.Theme(_ => Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty)
end NoColorPrinter
