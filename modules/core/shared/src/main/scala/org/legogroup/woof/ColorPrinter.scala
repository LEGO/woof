package org.legogroup.woof

import org.legogroup.woof.ColorPrinter.Theme
import org.legogroup.woof.ColorPrinter.Theme.{Background, Foreground, Formatting}

import java.text.{DateFormat, Format, SimpleDateFormat}
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Date
import scala.concurrent.duration.FiniteDuration

open class ColorPrinter(
    theme: Theme = Theme.defaultTheme,
    formatTime: EpochMillis => String = defaultTimeFormat,
) extends Printer:

  override def toPrint(
      epochMillis: EpochMillis,
      level: LogLevel,
      info: LogInfo,
      message: String,
      context: List[(String, String)],
  ): String =

    val levelColor   = theme.levelFormat(level)
    val postfixColor = theme.postfixFormat
    val reset        = theme.reset
    val prefix       = level.productPrefix.toUpperCase.padTo(5, ' ')
    val time         = formatTime(epochMillis)
    val contextPart =
      if context.isEmpty then ""
      else
        " " +
          context
            .map((key, value) => s"${theme.contextKey}$key${theme.reset}=${theme.contextValue}$value")
            .mkString(", ") + theme.reset.getCode
    s"$time $levelColor[$prefix]$reset$contextPart $postfixColor${info.prefix}$reset: $message $postfixColor${info.postfix}$reset"

  end toPrint

end ColorPrinter

object ColorPrinter:

  object Theme:
    import scala.Console

    val defaultTheme = Theme(
      level =>
        level match
          case LogLevel.Debug => Foreground.Green
          case LogLevel.Warn  => Foreground.Yellow.withStyle(Style.Bold)
          case LogLevel.Trace => Style.Underlined
          case LogLevel.Info  => Style.Underlined.withStyle(Style.Bold)
          case LogLevel.Error => Foreground.Red.withBackground(Background.White)
      ,
      Foreground.Magenta,
      Style.Reset,
      Style.Bold,
      Empty,
    )

    type Formatting = Foreground | Background | Style | Composite | Empty.type

    case object Empty:
      override def toString = ""

    extension (f: Formatting)
      def getCode: String = f match
        case fg: Foreground => fg.code
        case bg: Background => bg.code
        case st: Style      => st.code
        case cmp: Composite => cmp.code
        case Empty          => ""
      def withStyle(s: Style): Composite            = Composite(f.getCode + s.code)
      def withBackground(bg: Background): Composite = Composite(f.getCode + bg.code)
    end extension

    case class Composite(code: String):
      override def toString = code

    enum Style(val code: String):
      override def toString = code
      case Bold extends Style(Console.BOLD)
      case Underlined extends Style(Console.UNDERLINED)
      case Reversed extends Style(Console.REVERSED)
      case Invisible extends Style(Console.INVISIBLE)
      case Reset extends Style(Console.RESET)
    end Style

    enum Foreground(val code: String):
      override def toString = code
      case Black extends Foreground(Console.BLACK)
      case Green extends Foreground(Console.GREEN)
      case Yellow extends Foreground(Console.YELLOW)
      case Red extends Foreground(Console.RED)
      case Bold extends Foreground(Console.BOLD)
      case Blue extends Foreground(Console.BLUE)
      case Magenta extends Foreground(Console.MAGENTA)
      case Cyan extends Foreground(Console.CYAN)
      case White extends Foreground(Console.WHITE)
    end Foreground

    enum Background(val code: String):
      override def toString = code
      case Black extends Background(Console.BLACK_B)
      case Red extends Background(Console.RED_B)
      case Green extends Background(Console.GREEN_B)
      case Yellow extends Background(Console.YELLOW_B)
      case Blue extends Background(Console.BLUE_B)
      case Magenta extends Background(Console.MAGENTA_B)
      case Cyan extends Background(Console.CYAN_B)
      case White extends Background(Console.WHITE_B)
    end Background
  end Theme

  case class Theme(
      levelFormat: LogLevel => Formatting,
      postfixFormat: Formatting,
      reset: Formatting,
      contextKey: Formatting,
      contextValue: Formatting,
  )

end ColorPrinter
