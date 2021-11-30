package org.legogroup.woof

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, ZoneId}
import java.util.Locale

import cats.effect.IO
import cats.effect.kernel.Clock
import cats.effect.std.Console
import cats.kernel.Order
import cats.syntax.all.*
import cats.{FlatMap, Monad}
import org.legogroup.woof.local.Local

import Logger.*
import util.chaining.scalaUtilChainingOps
import Logging.LogInfo

/** This is the main interface for logging. Since we use macros for the methods, we cannot abstract this with an
  * interface. We make the class open to compensate.
  */
open class Logger[F[_]: StringLocal: Monad: Clock](output: Output[F], outputs: Output[F]*)(using Printer, Filter):

  val stringLocal: StringLocal[F] = summon[StringLocal[F]]
  val printer: Printer            = summon[Printer]
  val filter: Filter              = summon[Filter]

  private[woof] def makeLogString(
      level: LogLevel,
      info: Logging.LogInfo,
      message: String,
      context: List[(String, String)],
  ): F[String] =
    Clock[F].realTimeInstant.map(now => summon[Printer].toPrint(now, level, info, message, context))

  private[woof] def doOutputs(level: LogLevel, s: String): F[Unit] =
    val allOutputs = outputs.prepended(output)
    level match
      case LogLevel.Error => allOutputs.traverse_(_.outputError(s))
      case _              => allOutputs.traverse_(_.output(s))

  def doLog(level: LogLevel, message: String, logInfo: LogInfo): F[Unit] =
    for
      context <- summon[StringLocal[F]].ask
      logLine <- makeLogString(level, logInfo, message, context)
      _       <- doOutputs(level, logLine).whenA(summon[Filter](LogLine(level, logInfo, logLine, context)))
    yield ()

  inline def log(level: LogLevel, inline message: String): F[Unit] = doLog(level, message, Logging.info(message))

  inline def debug(inline message: String): F[Unit] = log(LogLevel.Debug, message)
  inline def info(inline message: String): F[Unit]  = log(LogLevel.Info, message)
  inline def trace(inline message: String): F[Unit] = log(LogLevel.Trace, message)
  inline def warn(inline message: String): F[Unit]  = log(LogLevel.Warn, message)
  inline def error(inline message: String): F[Unit] = log(LogLevel.Error, message)

end Logger

object Logger:

  extension [F[_]: Logger, A](fa: F[A])
    def withLogContext(key: String, value: String): F[A] =
      Logger[F].stringLocal.local(fa)(ctx => ctx.appended((key, value)))

  type StringLocal[F[_]] = Local[F, List[(String, String)]]

  def apply[F[_]](using l: Logger[F]): Logger[F] = l

  given Printer = ColorPrinter()

  val ioStringLocal = Local.makeIoLocal[List[(String, String)]]
  def makeIoLogger(output: Output[IO], outputs: Output[IO]*)(using Clock[IO], Printer, Filter): IO[Logger[IO]] =
    for given StringLocal[IO] <- ioStringLocal
    yield new Logger[IO](output, outputs*)

  enum LogLevel:
    case Trace, Debug, Info, Warn, Error
  given Order[LogLevel] = (x, y) => Order[Int].compare(x.ordinal, y.ordinal)

end Logger
