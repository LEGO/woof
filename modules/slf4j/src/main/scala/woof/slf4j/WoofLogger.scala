package woof.slf4j

import org.slf4j.Logger
import woof.Logger as WLogger
import cats.Id
import cats.effect.IO
import cats.effect.unsafe.IORuntime.global
import cats.effect.unsafe.IORuntime

class WoofLogger(name: String) extends Logger:
  import WoofLogger.given_IORuntime
  import WoofLogger.logger

  def info(msg: String): Unit  = logger.foreach(_.info(msg).unsafeRunSync())
  def debug(msg: String): Unit = logger.foreach(_.debug(msg).unsafeRunSync())
  def error(msg: String): Unit = logger.foreach(_.error(msg).unsafeRunSync())
  def trace(msg: String): Unit = logger.foreach(_.trace(msg).unsafeRunSync())
  def warn(msg: String): Unit  = logger.foreach(_.warn(msg).unsafeRunSync())

  def debug(x$0: String, x$1: Object): Unit                                     = ???
  def debug(x$0: String, x$1: Object, x$2: Object): Unit                        = ???
  def debug(x$0: String, x$1: Array[? <: Object]): Unit                         = ???
  def debug(x$0: String, x$1: Throwable): Unit                                  = ???
  def debug(x$0: org.slf4j.Marker, x$1: String): Unit                           = ???
  def debug(x$0: org.slf4j.Marker, x$1: String, x$2: Object): Unit              = ???
  def debug(x$0: org.slf4j.Marker, x$1: String, x$2: Object, x$3: Object): Unit = ???
  def debug(x$0: org.slf4j.Marker, x$1: String, x$2: Array[? <: Object]): Unit  = ???
  def debug(x$0: org.slf4j.Marker, x$1: String, x$2: Throwable): Unit           = ???
  def error(x$0: String, x$1: Object): Unit                                     = ???
  def error(x$0: String, x$1: Object, x$2: Object): Unit                        = ???
  def error(x$0: String, x$1: Array[? <: Object]): Unit                         = ???
  def error(x$0: String, x$1: Throwable): Unit                                  = ???
  def error(x$0: org.slf4j.Marker, x$1: String): Unit                           = ???
  def error(x$0: org.slf4j.Marker, x$1: String, x$2: Object): Unit              = ???
  def error(x$0: org.slf4j.Marker, x$1: String, x$2: Object, x$3: Object): Unit = ???
  def error(x$0: org.slf4j.Marker, x$1: String, x$2: Array[? <: Object]): Unit  = ???
  def error(x$0: org.slf4j.Marker, x$1: String, x$2: Throwable): Unit           = ???
  def getName(): String                                                         = name
  def info(x$0: String, x$1: Object): Unit                                      = ???
  def info(x$0: String, x$1: Object, x$2: Object): Unit                         = ???
  def info(x$0: String, x$1: Array[? <: Object]): Unit                          = ???
  def info(x$0: String, x$1: Throwable): Unit                                   = ???
  def info(x$0: org.slf4j.Marker, x$1: String): Unit                            = ???
  def info(x$0: org.slf4j.Marker, x$1: String, x$2: Object): Unit               = ???
  def info(x$0: org.slf4j.Marker, x$1: String, x$2: Object, x$3: Object): Unit  = ???
  def info(x$0: org.slf4j.Marker, x$1: String, x$2: Array[? <: Object]): Unit   = ???
  def info(x$0: org.slf4j.Marker, x$1: String, x$2: Throwable): Unit            = ???
  def isDebugEnabled(): Boolean                                                 = ???
  def isDebugEnabled(x$0: org.slf4j.Marker): Boolean                            = ???
  def isErrorEnabled(): Boolean                                                 = ???
  def isErrorEnabled(x$0: org.slf4j.Marker): Boolean                            = ???
  def isInfoEnabled(): Boolean                                                  = ???
  def isInfoEnabled(x$0: org.slf4j.Marker): Boolean                             = ???
  def isTraceEnabled(): Boolean                                                 = ???
  def isTraceEnabled(x$0: org.slf4j.Marker): Boolean                            = ???
  def isWarnEnabled(): Boolean                                                  = ???
  def isWarnEnabled(x$0: org.slf4j.Marker): Boolean                             = ???
  def trace(x$0: String, x$1: Object): Unit                                     = ???
  def trace(x$0: String, x$1: Object, x$2: Object): Unit                        = ???
  def trace(x$0: String, x$1: Array[? <: Object]): Unit                         = ???
  def trace(x$0: String, x$1: Throwable): Unit                                  = ???
  def trace(x$0: org.slf4j.Marker, x$1: String): Unit                           = ???
  def trace(x$0: org.slf4j.Marker, x$1: String, x$2: Object): Unit              = ???
  def trace(x$0: org.slf4j.Marker, x$1: String, x$2: Object, x$3: Object): Unit = ???
  def trace(x$0: org.slf4j.Marker, x$1: String, x$2: Array[? <: Object]): Unit  = ???
  def trace(x$0: org.slf4j.Marker, x$1: String, x$2: Throwable): Unit           = ???
  def warn(x$0: String, x$1: Object): Unit                                      = ???
  def warn(x$0: String, x$1: Array[? <: Object]): Unit                          = ???
  def warn(x$0: String, x$1: Object, x$2: Object): Unit                         = ???
  def warn(x$0: String, x$1: Throwable): Unit                                   = ???
  def warn(x$0: org.slf4j.Marker, x$1: String): Unit                            = ???
  def warn(x$0: org.slf4j.Marker, x$1: String, x$2: Object): Unit               = ???
  def warn(x$0: org.slf4j.Marker, x$1: String, x$2: Object, x$3: Object): Unit  = ???
  def warn(x$0: org.slf4j.Marker, x$1: String, x$2: Array[? <: Object]): Unit   = ???
  def warn(x$0: org.slf4j.Marker, x$1: String, x$2: Throwable): Unit            = ???

end WoofLogger

object WoofLogger:
  private given IORuntime         = global
  var logger: Option[WLogger[IO]] = None
end WoofLogger
