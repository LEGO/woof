package clog

import java.time.format.DateTimeFormatter
import java.time.Instant
import Logger.LogLevel
import java.time.ZoneId
import java.text.Format
import ColorPrinter.Theme.Foreground
import ColorPrinter.Theme.Background
import ColorPrinter.Theme.Formatting
import ColorPrinter.Theme

open class ColorPrinter(theme: Theme = Theme.defaultTheme, zoneId: ZoneId = ZoneId.systemDefault()) extends Printer:

  private val dateTimeFormatter = DateTimeFormatter
    .ofPattern("HH:mm:ss")
    .withZone(zoneId);

  override def toPrint(instant: Instant, level: LogLevel, info: Logging.LogInfo, message: String): String =

    val levelColor   = theme.levelFormat(level)
    val postfixColor = theme.postfixFormat
    val reset        = Theme.Style.Reset
    val prefix       = level.productPrefix.toUpperCase.padTo(5, ' ')
    val time         = dateTimeFormatter.format(instant)
    s"$time $levelColor[$prefix]$reset $postfixColor${info.prefix}$reset: $message $postfixColor${info.postfix}$reset"

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
          case LogLevel.Info  => Style.Underlined.withStyle(Style.Bold)
          case LogLevel.Error => Foreground.Red.withBackground(Background.White)
      ,
      Foreground.Magenta,
    )

    type Formatting = Foreground | Background | Style | Composite

    extension (f: Formatting)
      def getCode: String = f match
        case fg: Foreground => fg.code
        case bg: Background => bg.code
        case st: Style      => st.code
        case cmp: Composite => cmp.code
      def withStyle(s: Style): Composite            = Composite(f.getCode + s.code)
      def withBackground(bg: Background): Composite = Composite(f.getCode + bg.code)

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
      case Yellow extends Foreground(Console.GREEN)
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

  case class Theme(levelFormat: LogLevel => Formatting, postfixFormat: Formatting)

end ColorPrinter
