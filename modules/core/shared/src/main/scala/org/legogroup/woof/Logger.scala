package org.legogroup.woof

import cats.effect.IO
import cats.effect.kernel.Clock
import cats.effect.std.Console
import cats.kernel.Order
import cats.syntax.all.*
import cats.{FlatMap, Monad}
import org.legogroup.woof.Logger.*
import org.legogroup.woof.local.Local

import java.time.format.{DateTimeFormatter, FormatStyle}
import java.time.{Instant, ZoneId}
import java.util.Locale
import scala.util.chaining.scalaUtilChainingOps

trait Logger[F[_]]:

  val stringLocal: StringLocal[F]

  def doLog(level: LogLevel, message: String)(using LogInfo): F[Unit]
  def debug(message: String)(using LogInfo): F[Unit] = doLog(LogLevel.Debug, message)
  def info(message: String)(using LogInfo): F[Unit]  = doLog(LogLevel.Info, message)
  def trace(message: String)(using LogInfo): F[Unit] = doLog(LogLevel.Trace, message)
  def warn(message: String)(using LogInfo): F[Unit]  = doLog(LogLevel.Warn, message)
  def error(message: String)(using LogInfo): F[Unit] = doLog(LogLevel.Error, message)

end Logger

class DefaultLogger[F[_]: StringLocal: Monad: Clock](output: Output[F], outputs: Output[F]*)(using Printer, Filter)
    extends Logger[F]:

  val stringLocal: StringLocal[F] = summon[StringLocal[F]]
  val printer: Printer            = summon[Printer]
  val filter: Filter              = summon[Filter]

  private[woof] def makeLogString(
      level: LogLevel,
      info: LogInfo,
      message: String,
      context: List[(String, String)],
  ): F[String] =
    Clock[F].realTime
      .map(d => EpochMillis(d.toMillis))
      .map(now => summon[Printer].toPrint(now, level, info, message, context))

  private[woof] def doOutputs(level: LogLevel, s: String): F[Unit] =
    val allOutputs = outputs.prepended(output)
    level match
      case LogLevel.Error => allOutputs.traverse_(_.outputError(s))
      case _              => allOutputs.traverse_(_.output(s))

  override def doLog(level: LogLevel, message: String)(using logInfo: LogInfo): F[Unit] =
    for
      context <- summon[StringLocal[F]].ask
      logLine <- makeLogString(level, logInfo, message, context)
      _       <- doOutputs(level, logLine).whenA(summon[Filter](LogLine(level, logInfo, logLine, context)))
    yield ()

end DefaultLogger

object DefaultLogger:
  def makeIo(output: Output[IO], outputs: Output[IO]*)(using Clock[IO], Printer, Filter): IO[DefaultLogger[IO]] =
    for given StringLocal[IO] <- ioStringLocal
    yield new DefaultLogger[IO](output, outputs*)
end DefaultLogger

object Logger:

  extension [F[_]: Logger, A](fa: F[A])
    def withLogContext(key: String, value: String): F[A] =
      Logger[F].stringLocal.local(fa)(ctx => ctx.appended((key, value)))

  type StringLocal[F[_]] = Local[F, List[(String, String)]]

  def apply[F[_]](using l: Logger[F]): Logger[F] = l

  given Printer = ColorPrinter()

  val ioStringLocal = Local.makeIoLocal[List[(String, String)]]

  @deprecated(s"Use `DefaultLogger.makeIo`") def makeIoLogger(output: Output[IO], outputs: Output[IO]*)(using
      Clock[IO],
      Printer,
      Filter,
  ): IO[Logger[IO]] =
    for given StringLocal[IO] <- ioStringLocal
    yield new DefaultLogger[IO](output, outputs*)

end Logger
