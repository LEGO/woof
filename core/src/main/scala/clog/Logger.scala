package clog

import cats.effect.std.Console
import Logger.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.time.ZoneId
import cats.syntax.all.*
import cats.effect.kernel.Clock
import cats.FlatMap
import util.chaining.scalaUtilChainingOps
import cats.Monad

trait Output[F[_]]:
  def output(str: String): F[Unit]
  def outputError(str: String): F[Unit]
end Output
object Output:
  def fromConsole[F[_]: Console]: Output[F] = new Output[F]:
    def output(str: String): F[Unit]      = Console[F].println(str)
    def outputError(str: String): F[Unit] = Console[F].errorln(str)
end Output

class Logger[F[_]: Monad: Clock](output: Output[F], outputs: Output[F]*)(using Printer):

  private[clog] def makeLogLine(level: LogLevel, info: Logging.LogInfo, message: String): F[String] =
    Clock[F].realTimeInstant.map(now => summon[Printer].toPrint(now, level, info, message))

  inline def log(level: LogLevel, inline message: String): F[Unit] =
    val info = Logging.info(message)
    val doOutput: String => F[Unit] = (s: String) =>
      level match
        case LogLevel.Error => (outputs.prepended(output)).traverse(_.outputError(s)).map(_.combineAll)
        case _              => (outputs.prepended(output)).traverse(_.output(s)).map(_.combineAll)
    makeLogLine(level, info, message) >>= doOutput
  end log

  inline def debug(inline message: String): F[Unit] = log(LogLevel.Debug, message)
  inline def info(inline message: String): F[Unit]  = log(LogLevel.Info, message)
  inline def warn(inline message: String): F[Unit]  = log(LogLevel.Warn, message)
  inline def error(inline message: String): F[Unit] = log(LogLevel.Error, message)

end Logger

object Logger:

  def apply[F[_]](using l: Logger[F]): Logger[F] = l

  given Printer = ColorPrinter()

  enum LogLevel:
    case Debug, Info, Warn, Error

end Logger
