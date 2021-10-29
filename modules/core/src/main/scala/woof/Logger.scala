package woof

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
import woof.local.Local
import cats.effect.IO
import cats.kernel.Order

open class Logger[F[_]: StringLocal: Monad: Clock](output: Output[F], outputs: Output[F]*)(using Printer, Filter):

  private[woof] val stringLocal: StringLocal[F] = summon[StringLocal[F]]

  private[woof] def makeLogString(
      level: LogLevel,
      info: Logging.LogInfo,
      message: String,
      context: List[(String, String)],
  ): F[String] =
    Clock[F].realTimeInstant.map(now => summon[Printer].toPrint(now, level, info, message, context))

  private def doOutputs(level: LogLevel, s: String) =
    val allOutputs = outputs.prepended(output)
    level match
      case LogLevel.Error => allOutputs.traverse_(_.outputError(s))
      case _              => allOutputs.traverse_(_.output(s))

  inline def log(level: LogLevel, inline message: String): F[Unit] =
    val info = Logging.info(message)
    for
      context <- summon[StringLocal[F]].ask
      logLine <- makeLogString(level, info, message, context)
      _       <- doOutputs(level, logLine).whenA(summon[Filter](LogLine(level, info, logLine, context)))
    yield ()
  end log

  inline def debug(inline message: String): F[Unit] = log(LogLevel.Debug, message)
  inline def info(inline message: String): F[Unit]  = log(LogLevel.Info, message)
  inline def trace(inline message: String): F[Unit] = log(LogLevel.Info, message)
  inline def warn(inline message: String): F[Unit]  = log(LogLevel.Warn, message)
  inline def error(inline message: String): F[Unit] = log(LogLevel.Error, message)

end Logger

object Logger:

  extension [F[_]: Logger, A](fa: F[A])
    def withLogContext(key: String, value: String): F[A] = Logger[F].stringLocal.local(fa)(ctx => (key, value) :: ctx)

  type Kvp               = (String, String)
  type StringLocal[F[_]] = Local[F, List[Kvp]]

  def apply[F[_]](using l: Logger[F]): Logger[F] = l

  given Printer = ColorPrinter()

  val ioStringLocal = Local.makeIoLocal[List[(String, String)]]
  def makeIoLogger(output: Output[IO], outputs: Output[IO]*)(using Clock[IO], Printer, Filter): IO[Logger[IO]] =
    for given StringLocal[IO] <- ioStringLocal
    yield new Logger[IO](output, outputs*)

  enum LogLevel:
    case Debug, Info, Trace, Warn, Error
  given Order[LogLevel] = (x, y) => Order[Int].compare(x.ordinal, y.ordinal)

end Logger
