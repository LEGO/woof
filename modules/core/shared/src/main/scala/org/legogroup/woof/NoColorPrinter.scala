package org.legogroup.woof

import org.legogroup.woof.ColorPrinter.Theme

import scala.concurrent.duration.FiniteDuration
import scala.util.chaining.scalaUtilChainingOps

class NoColorPrinter(formatTime: EpochMillis => String = defaultTimeFormat)
    extends ColorPrinter(theme = NoColorPrinter.noColorTheme, formatTime = formatTime):
end NoColorPrinter

object NoColorPrinter:
  val noColorTheme = ColorPrinter.Theme(_ => Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty, Theme.Empty)
end NoColorPrinter
