package woof.slf4j

import org.slf4j.Logger
import woof.Logger as WLogger
import cats.Id
import cats.effect.IO
import cats.effect.unsafe.IORuntime.global
import cats.effect.unsafe.IORuntime
import woof.Logging.LogInfo
import javassist.ClassPool
import java.io.File
import woof.Logger.LogLevel
import woof.LogLine
class WoofLogger(name: String) extends Logger:

  import WoofLogger.given_IORuntime
  import WoofLogger.logger

  private def getLogInfo() =
    val stacktraceElements = Thread.currentThread().getStackTrace()
    val callingMethodIndex =
      stacktraceElements.indexWhere(s =>
        val cm       = s.getMethodName
        val cn       = s.getClassName
        val isMethod = List("info", "trace", "warn", "debug", "error").contains(cm)
        val isClass  = cn.equalsIgnoreCase("woof.slf4j.WoofLogger")
        isClass && isMethod,
      ) + 1 // go 1 up in the stack
    val callingMethod: StackTraceElement = stacktraceElements(callingMethodIndex)
    val className                        = callingMethod.getClassName
    val fileName                         = className.replace('.', '/') + ".scala"
    val lineNumber                       = callingMethod.getLineNumber - 1
    LogInfo(className, new File(fileName), lineNumber)
  end getLogInfo

  private def log(level: LogLevel, msg: String) = logger.foreach(_.doLog(level, msg, getLogInfo()).unsafeRunSync())
  def info(msg: String): Unit                   = log(LogLevel.Info, msg)
  def debug(msg: String): Unit                  = log(LogLevel.Debug, msg)
  def error(msg: String): Unit                  = log(LogLevel.Error, msg)
  def trace(msg: String): Unit                  = log(LogLevel.Trace, msg)
  def warn(msg: String): Unit                   = log(LogLevel.Warn, msg)

  def debug(msg: String, obj: Object): Unit                        = debug(s"$msg, $obj")
  def debug(msg: String, obj1: Object, obj2: Object): Unit         = debug(s"$msg, $obj1, $obj2")
  def debug(msg: String, objs: Array[? <: Object]): Unit           = debug(s"$msg, ${objs.mkString(", ")}")
  def debug(msg: String, throwable: Throwable): Unit               = debug(s"$msg, ${throwable.getMessage}")
  def debug(x$0: org.slf4j.Marker, msg: String): Unit              = ???
  def debug(x$0: org.slf4j.Marker, msg: String, x$2: Object): Unit = ???
  def debug(x$0: org.slf4j.Marker, msg: String, x$2: Object, x$3: Object): Unit = ???
  def debug(x$0: org.slf4j.Marker, msg: String, x$2: Array[? <: Object]): Unit  = ???
  def debug(x$0: org.slf4j.Marker, msg: String, x$2: Throwable): Unit           = ???

  def error(msg: String, obj: Object): Unit                        = error(s"$msg, $obj")
  def error(msg: String, obj1: Object, obj2: Object): Unit         = error(s"$msg, $obj1, $obj2")
  def error(msg: String, objs: Array[? <: Object]): Unit           = error(s"$msg, ${objs.mkString(", ")}")
  def error(msg: String, throwable: Throwable): Unit               = error(s"$msg, ${throwable.getMessage}")
  def error(x$0: org.slf4j.Marker, msg: String): Unit              = ???
  def error(x$0: org.slf4j.Marker, msg: String, x$2: Object): Unit = ???
  def error(x$0: org.slf4j.Marker, msg: String, x$2: Object, x$3: Object): Unit = ???
  def error(x$0: org.slf4j.Marker, msg: String, x$2: Array[? <: Object]): Unit  = ???
  def error(x$0: org.slf4j.Marker, msg: String, x$2: Throwable): Unit           = ???
  def getName(): String                                                         = name

  def info(msg: String, obj: Object): Unit                                     = info(s"$msg, $obj")
  def info(msg: String, obj1: Object, obj2: Object): Unit                      = info(s"$msg, $obj1, $obj2")
  def info(msg: String, objs: Array[? <: Object]): Unit                        = info(s"$msg, ${objs.mkString(", ")}")
  def info(msg: String, throwable: Throwable): Unit                            = info(s"$msg, ${throwable.getMessage}")
  def info(x$0: org.slf4j.Marker, msg: String): Unit                           = ???
  def info(x$0: org.slf4j.Marker, msg: String, x$2: Object): Unit              = ???
  def info(x$0: org.slf4j.Marker, msg: String, x$2: Object, x$3: Object): Unit = ???
  def info(x$0: org.slf4j.Marker, msg: String, x$2: Array[? <: Object]): Unit  = ???
  def info(x$0: org.slf4j.Marker, msg: String, x$2: Throwable): Unit           = ???

  private def testLevel(logLevel: LogLevel): Boolean = logger.exists(_.filter(LogLine(logLevel, getLogInfo(), "", Nil)))
  def isDebugEnabled(): Boolean                      = testLevel(LogLevel.Debug)
  def isErrorEnabled(): Boolean                      = testLevel(LogLevel.Error)
  def isInfoEnabled(): Boolean                       = testLevel(LogLevel.Info)
  def isTraceEnabled(): Boolean                      = testLevel(LogLevel.Trace)
  def isWarnEnabled(): Boolean                       = testLevel(LogLevel.Warn)

  def isInfoEnabled(x$0: org.slf4j.Marker): Boolean  = ???
  def isTraceEnabled(x$0: org.slf4j.Marker): Boolean = ???
  def isDebugEnabled(x$0: org.slf4j.Marker): Boolean = ???
  def isErrorEnabled(x$0: org.slf4j.Marker): Boolean = ???
  def isWarnEnabled(x$0: org.slf4j.Marker): Boolean  = ???

  def trace(msg: String, obj: Object): Unit                        = trace(s"$msg, $obj")
  def trace(msg: String, obj1: Object, obj2: Object): Unit         = trace(s"$msg, $obj1, $obj2")
  def trace(msg: String, objs: Array[? <: Object]): Unit           = trace(s"$msg, ${objs.mkString(", ")}")
  def trace(msg: String, throwable: Throwable): Unit               = trace(s"$msg, ${throwable.getMessage}")
  def trace(x$0: org.slf4j.Marker, msg: String): Unit              = ???
  def trace(x$0: org.slf4j.Marker, msg: String, x$2: Object): Unit = ???
  def trace(x$0: org.slf4j.Marker, msg: String, x$2: Object, x$3: Object): Unit = ???
  def trace(x$0: org.slf4j.Marker, msg: String, x$2: Array[? <: Object]): Unit  = ???
  def trace(x$0: org.slf4j.Marker, msg: String, x$2: Throwable): Unit           = ???

  def warn(msg: String, obj: Object): Unit                                     = warn(s"$msg, $obj")
  def warn(msg: String, obj1: Object, obj2: Object): Unit                      = warn(s"$msg, $obj1, $obj2")
  def warn(msg: String, objs: Array[? <: Object]): Unit                        = warn(s"$msg, ${objs.mkString(", ")}")
  def warn(msg: String, throwable: Throwable): Unit                            = warn(s"$msg, ${throwable.getMessage}")
  def warn(x$0: org.slf4j.Marker, msg: String): Unit                           = ???
  def warn(x$0: org.slf4j.Marker, msg: String, x$2: Object): Unit              = ???
  def warn(x$0: org.slf4j.Marker, msg: String, x$2: Object, x$3: Object): Unit = ???
  def warn(x$0: org.slf4j.Marker, msg: String, x$2: Array[? <: Object]): Unit  = ???
  def warn(x$0: org.slf4j.Marker, msg: String, x$2: Throwable): Unit           = ???

end WoofLogger

object WoofLogger:
  private given IORuntime         = global
  var logger: Option[WLogger[IO]] = None
end WoofLogger
