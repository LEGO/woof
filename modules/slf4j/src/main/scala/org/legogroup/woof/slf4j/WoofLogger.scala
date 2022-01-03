package org.legogroup.woof.slf4j

import cats.Id
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import cats.effect.unsafe.IORuntime.global
import org.legogroup.woof.{EnclosingClass, LogInfo, LogLevel, LogLine, Logger as WLogger}
import org.slf4j.Logger

import java.io.File
class WoofLogger(name: String) extends Logger:

  import WoofLogger.{given_IORuntime, logger}

  private def getLogInfo() =
    val stacktraceElements = Thread.currentThread().getStackTrace()
    val callingMethodIndex =
      stacktraceElements.size - stacktraceElements.reverse.indexWhere(s => s.getClassName == this.getClass.getName,
      ) // after last mention of this class
    val callingMethod: StackTraceElement = stacktraceElements(callingMethodIndex)
    val enclosingClassName               = EnclosingClass(callingMethod.getClassName)
    val fileName                         = (enclosingClassName.fullName.replace('.', '/') + ".scala").split("\\/").takeRight(1).mkString
    val lineNumber                       = callingMethod.getLineNumber - 1
    LogInfo(enclosingClassName, fileName, lineNumber)
  end getLogInfo

  def getName(): String = name

  private def log(level: LogLevel, msg: String) = logger.foreach(_.doLog(level, msg, getLogInfo()).unsafeRunSync())
  def info(msg: String): Unit                   = log(LogLevel.Info, msg)
  def debug(msg: String): Unit                  = log(LogLevel.Debug, msg)
  def error(msg: String): Unit                  = log(LogLevel.Error, msg)
  def trace(msg: String): Unit                  = log(LogLevel.Trace, msg)
  def warn(msg: String): Unit                   = log(LogLevel.Warn, msg)

  def debug(msg: String, obj: Object): Unit                        = debug(s"$msg $obj")
  def debug(msg: String, obj1: Object, obj2: Object): Unit         = debug(s"$msg $obj1, $obj2")
  def debug(msg: String, objs: Array[? <: Object]): Unit           = debug(s"$msg ${objs.mkString(", ")}")
  def debug(msg: String, throwable: Throwable): Unit               = debug(s"$msg ${throwable.getMessage}")
  def debug(x$0: org.slf4j.Marker, msg: String): Unit              = debug(msg)
  def debug(x$0: org.slf4j.Marker, msg: String, obj: Object): Unit = debug(s"$msg, $obj")
  def debug(x$0: org.slf4j.Marker, msg: String, obj1: Object, obj2: Object): Unit = debug(s"$msg, $obj1, $obj2")
  def debug(x$0: org.slf4j.Marker, msg: String, objs: Array[? <: Object]): Unit = debug(s"$msg ${objs.mkString(", ")}")
  def debug(x$0: org.slf4j.Marker, msg: String, throwable: Throwable): Unit     = debug(s"$msg ${throwable.getMessage}")

  def error(msg: String, obj: Object): Unit                        = error(s"$msg $obj")
  def error(msg: String, obj1: Object, obj2: Object): Unit         = error(s"$msg $obj1, $obj2")
  def error(msg: String, objs: Array[? <: Object]): Unit           = error(s"$msg ${objs.mkString(", ")}")
  def error(msg: String, throwable: Throwable): Unit               = error(s"$msg ${throwable.getMessage}")
  def error(x$0: org.slf4j.Marker, msg: String): Unit              = error(msg)
  def error(x$0: org.slf4j.Marker, msg: String, obj: Object): Unit = error(s"$msg $obj")
  def error(x$0: org.slf4j.Marker, msg: String, obj1: Object, obj2: Object): Unit = error(s"$msg $obj1, $obj2")
  def error(x$0: org.slf4j.Marker, msg: String, objs: Array[? <: Object]): Unit = error(s"$msg ${objs.mkString(", ")}")
  def error(x$0: org.slf4j.Marker, msg: String, throwable: Throwable): Unit     = error(s"$msg ${throwable.getMessage}")

  def info(msg: String, obj: Object): Unit                                       = info(s"$msg, $obj")
  def info(msg: String, obj1: Object, obj2: Object): Unit                        = info(s"$msg, $obj1, $obj2")
  def info(msg: String, objs: Array[? <: Object]): Unit                          = info(s"$msg ${objs.mkString(", ")}")
  def info(msg: String, throwable: Throwable): Unit                              = info(s"$msg ${throwable.getMessage}")
  def info(x$0: org.slf4j.Marker, msg: String): Unit                             = info(msg)
  def info(x$0: org.slf4j.Marker, msg: String, obj: Object): Unit                = info(s"$msg, $obj")
  def info(x$0: org.slf4j.Marker, msg: String, obj1: Object, obj2: Object): Unit = info(s"$msg, $obj1, $obj2")
  def info(x$0: org.slf4j.Marker, msg: String, objs: Array[? <: Object]): Unit   = info(s"$msg ${objs.mkString(", ")}")
  def info(x$0: org.slf4j.Marker, msg: String, throwable: Throwable): Unit       = info(s"$msg ${throwable.getMessage}")

  def trace(msg: String, obj: Object): Unit                        = trace(s"$msg, $obj")
  def trace(msg: String, obj1: Object, obj2: Object): Unit         = trace(s"$msg, $obj1, $obj2")
  def trace(msg: String, objs: Array[? <: Object]): Unit           = trace(s"$msg ${objs.mkString(", ")}")
  def trace(msg: String, throwable: Throwable): Unit               = trace(s"$msg ${throwable.getMessage}")
  def trace(x$0: org.slf4j.Marker, msg: String): Unit              = trace(msg)
  def trace(x$0: org.slf4j.Marker, msg: String, obj: Object): Unit = trace(s"$msg, $obj")
  def trace(x$0: org.slf4j.Marker, msg: String, obj1: Object, obj2: Object): Unit = trace(s"$msg, $obj1, $obj2")
  def trace(x$0: org.slf4j.Marker, msg: String, objs: Array[? <: Object]): Unit = trace(s"$msg ${objs.mkString(", ")}")
  def trace(x$0: org.slf4j.Marker, msg: String, throwable: Throwable): Unit     = trace(s"$msg ${throwable.getMessage}")

  def warn(msg: String, obj: Object): Unit                                       = warn(s"$msg, $obj")
  def warn(msg: String, obj1: Object, obj2: Object): Unit                        = warn(s"$msg, $obj1, $obj2")
  def warn(msg: String, objs: Array[? <: Object]): Unit                          = warn(s"$msg ${objs.mkString(", ")}")
  def warn(msg: String, throwable: Throwable): Unit                              = warn(s"$msg ${throwable.getMessage}")
  def warn(x$0: org.slf4j.Marker, msg: String): Unit                             = warn(msg)
  def warn(x$0: org.slf4j.Marker, msg: String, obj: Object): Unit                = warn(s"$msg, $obj")
  def warn(x$0: org.slf4j.Marker, msg: String, obj1: Object, obj2: Object): Unit = warn(s"$msg, $obj1, $obj2")
  def warn(x$0: org.slf4j.Marker, msg: String, objs: Array[? <: Object]): Unit   = warn(s"$msg ${objs.mkString(", ")}")
  def warn(x$0: org.slf4j.Marker, msg: String, throwable: Throwable): Unit       = warn(s"$msg ${throwable.getMessage}")

  /** Since woof allows much more complex filters, the best we can do is probe the filter with the requested log level
    * and arbitrary values in the rest of the log line.
    */
  private def testLevel(logLevel: LogLevel): Boolean =
    val mockLogLine = LogLine(logLevel, LogInfo(EnclosingClass(""), "", -1), "", Nil)
    logger.exists(_.filter(mockLogLine))

  def isDebugEnabled(): Boolean = testLevel(LogLevel.Debug)
  def isErrorEnabled(): Boolean = testLevel(LogLevel.Error)
  def isInfoEnabled(): Boolean  = testLevel(LogLevel.Info)
  def isTraceEnabled(): Boolean = testLevel(LogLevel.Trace)
  def isWarnEnabled(): Boolean  = testLevel(LogLevel.Warn)

  def isInfoEnabled(x$0: org.slf4j.Marker): Boolean  = isInfoEnabled()
  def isTraceEnabled(x$0: org.slf4j.Marker): Boolean = isTraceEnabled()
  def isDebugEnabled(x$0: org.slf4j.Marker): Boolean = isDebugEnabled()
  def isErrorEnabled(x$0: org.slf4j.Marker): Boolean = isErrorEnabled()
  def isWarnEnabled(x$0: org.slf4j.Marker): Boolean  = isWarnEnabled()

end WoofLogger

object WoofLogger:
  private given IORuntime         = global
  var logger: Option[WLogger[IO]] = None
end WoofLogger

extension (w: WLogger[IO]) def registerSlf4j: IO[Unit] = IO.delay(WoofLogger.logger = Some(w))
