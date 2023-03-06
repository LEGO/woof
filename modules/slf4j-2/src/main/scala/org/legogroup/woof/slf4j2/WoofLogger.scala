package org.legogroup.woof.slf4j2

import cats.Id
import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.effect.unsafe.IORuntime
import cats.effect.unsafe.IORuntime.global
import org.legogroup.woof.slf4j.Slf4jWoofLoggerImpl
import org.legogroup.woof.{EnclosingClass, LogInfo, LogLevel, LogLine, Logger as WLogger}
import org.slf4j.{Logger, Marker}

import java.io.File
import scala.util.Try

class WoofLogger(name: String) extends Logger with Slf4jWoofLoggerImpl[IO, Marker](name):
  override def logger: Option[WLogger[IO]]        = WoofLogger.logger
  override def dispatcher: Option[Dispatcher[IO]] = WoofLogger.dispatcher
end WoofLogger

object WoofLogger:
  var logger: Option[WLogger[IO]]        = None
  var dispatcher: Option[Dispatcher[IO]] = None
end WoofLogger

extension (w: WLogger[IO])
  def registerSlf4j(using d: Dispatcher[IO]): IO[Unit] = IO.delay {
    WoofLogger.logger = Some(w)
    WoofLogger.dispatcher = Some(d)
  }
