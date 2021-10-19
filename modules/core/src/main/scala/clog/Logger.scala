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
import clog.local.Local
import cats.effect.IO

open class Logger[F[_]: StringLocal: Monad: Clock](output: Output[F], outputs: Output[F]*)(using Printer):

  private[clog] def makeLogLine(
      level: LogLevel,
      info: Logging.LogInfo,
      message: String,
      context: List[(String, String)],
  ): F[String] =
    Clock[F].realTimeInstant.map(now => summon[Printer].toPrint(now, level, info, message, context))

  inline def log(level: LogLevel, inline message: String): F[Unit] =
    val info       = Logging.info(message)
    val allOutputs = outputs.prepended(output)
    val doOutputs: String => F[Unit] = (s: String) =>
      level match
        case LogLevel.Error => allOutputs.traverse_(_.outputError(s))
        case _              => allOutputs.traverse_(_.output(s))
    for
      context <- summon[StringLocal[F]].ask
      logLine <- makeLogLine(level, info, message, context)
      _       <- doOutputs(logLine)
    yield ()
  end log

  inline def debug(inline message: String): F[Unit] = log(LogLevel.Debug, message)
  inline def info(inline message: String): F[Unit]  = log(LogLevel.Info, message)
  inline def warn(inline message: String): F[Unit]  = log(LogLevel.Warn, message)
  inline def error(inline message: String): F[Unit] = log(LogLevel.Error, message)

end Logger

object Logger:

  type Kvp               = (String, String)
  type StringLocal[F[_]] = Local[F, List[Kvp]]

  def apply[F[_]](using l: Logger[F]): Logger[F] = l

  given Printer = ColorPrinter()
  def makeIoLogger(using Clock[IO], Printer): IO[Logger[IO]] =
    for given StringLocal[IO] <- Local.makeIoLocal[List[(String, String)]]
    yield new Logger[IO](Output.fromConsole)

  enum LogLevel:
    case Debug, Info, Warn, Error

end Logger
