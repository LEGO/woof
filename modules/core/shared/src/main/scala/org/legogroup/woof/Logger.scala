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

object Logger:

  extension [F[_]: Logger, A](fa: F[A])
    def withLogContext(key: String, value: String): F[A] =
      Logger[F].stringLocal.local(fa)(_.appended((key, value)))

    def withLogContext(keyValuePairs: (String, String)*): F[A] =
      Logger[F].stringLocal.local(fa)(_ ++ keyValuePairs)

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
