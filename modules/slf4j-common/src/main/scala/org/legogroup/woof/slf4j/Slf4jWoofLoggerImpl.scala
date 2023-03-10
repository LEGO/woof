package org.legogroup.woof.slf4j

import cats.Functor
import cats.effect.std.Dispatcher
import org.legogroup.woof.*
import cats.syntax.all.*

import scala.util.Try

trait Slf4jWoofLoggerImpl[F[_], Marker](name: String):

  private def getLogInfo() =
    val stacktraceElements = (new Throwable).getStackTrace()
    val lastIndex = stacktraceElements.reverse.indexWhere(s =>
      s.getClassName == this.getClass.getName
    ) // after last mention of this class
    val callingMethodIndex               = stacktraceElements.size - lastIndex
    val callingMethod: StackTraceElement = stacktraceElements(callingMethodIndex)
    val enclosingClassName               = EnclosingClass(callingMethod.getClassName)
    val fileName   = (enclosingClassName.fullName.replace('.', '/') + ".scala").split("\\/").takeRight(1).mkString
    val lineNumber = callingMethod.getLineNumber - 1
    LogInfo(enclosingClassName, fileName, lineNumber)
  end getLogInfo

  def getName(): String = name

  def logger: Option[Logger[F]]
  def dispatcher: Option[Dispatcher[F]]

  private def log(level: LogLevel, msg: String): Unit =
    (logger, dispatcher).mapN { (l, dispatcher) =>
      val logInfo = getLogInfo()
      val _       = dispatcher.unsafeRunSync(l.doLog(level, msg)(using logInfo))
    }

  def info(msg: String): Unit  = log(LogLevel.Info, msg)
  def debug(msg: String): Unit = log(LogLevel.Debug, msg)
  def error(msg: String): Unit = log(LogLevel.Error, msg)
  def trace(msg: String): Unit = log(LogLevel.Trace, msg)
  def warn(msg: String): Unit  = log(LogLevel.Warn, msg)

  private def throwableMessage(msg: String, throwable: Throwable) = s"$msg " + Try(throwable.getMessage).getOrElse("")

  def debug(msg: String, obj: Object): Unit                             = debug(s"$msg $obj")
  def debug(msg: String, obj1: Object, obj2: Object): Unit              = debug(s"$msg $obj1, $obj2")
  def debug(msg: String, objs: Array[? <: Object]): Unit                = debug(s"$msg ${objs.mkString(", ")}")
  def debug(msg: String, throwable: Throwable): Unit                    = debug(throwableMessage(msg, throwable))
  def debug(x$0: Marker, msg: String): Unit                             = debug(msg)
  def debug(x$0: Marker, msg: String, obj: Object): Unit                = debug(s"$msg, $obj")
  def debug(x$0: Marker, msg: String, obj1: Object, obj2: Object): Unit = debug(s"$msg, $obj1, $obj2")
  def debug(x$0: Marker, msg: String, objs: Array[? <: Object]): Unit   = debug(s"$msg ${objs.mkString(", ")}")
  def debug(x$0: Marker, msg: String, throwable: Throwable): Unit       = debug(throwableMessage(msg, throwable))

  def error(msg: String, obj: Object): Unit                             = error(s"$msg $obj")
  def error(msg: String, obj1: Object, obj2: Object): Unit              = error(s"$msg $obj1, $obj2")
  def error(msg: String, objs: Array[? <: Object]): Unit                = error(s"$msg ${objs.mkString(", ")}")
  def error(msg: String, throwable: Throwable): Unit                    = error(throwableMessage(msg, throwable))
  def error(x$0: Marker, msg: String): Unit                             = error(msg)
  def error(x$0: Marker, msg: String, obj: Object): Unit                = error(s"$msg $obj")
  def error(x$0: Marker, msg: String, obj1: Object, obj2: Object): Unit = error(s"$msg $obj1, $obj2")
  def error(x$0: Marker, msg: String, objs: Array[? <: Object]): Unit   = error(s"$msg ${objs.mkString(", ")}")
  def error(x$0: Marker, msg: String, throwable: Throwable): Unit       = error(throwableMessage(msg, throwable))

  def info(msg: String, obj: Object): Unit                             = info(s"$msg, $obj")
  def info(msg: String, obj1: Object, obj2: Object): Unit              = info(s"$msg, $obj1, $obj2")
  def info(msg: String, objs: Array[? <: Object]): Unit                = info(s"$msg ${objs.mkString(", ")}")
  def info(msg: String, throwable: Throwable): Unit                    = info(throwableMessage(msg, throwable))
  def info(x$0: Marker, msg: String): Unit                             = info(msg)
  def info(x$0: Marker, msg: String, obj: Object): Unit                = info(s"$msg, $obj")
  def info(x$0: Marker, msg: String, obj1: Object, obj2: Object): Unit = info(s"$msg, $obj1, $obj2")
  def info(x$0: Marker, msg: String, objs: Array[? <: Object]): Unit   = info(s"$msg ${objs.mkString(", ")}")
  def info(x$0: Marker, msg: String, throwable: Throwable): Unit       = info(throwableMessage(msg, throwable))

  def trace(msg: String, obj: Object): Unit                             = trace(s"$msg, $obj")
  def trace(msg: String, obj1: Object, obj2: Object): Unit              = trace(s"$msg, $obj1, $obj2")
  def trace(msg: String, objs: Array[? <: Object]): Unit                = trace(s"$msg ${objs.mkString(", ")}")
  def trace(msg: String, throwable: Throwable): Unit                    = trace(throwableMessage(msg, throwable))
  def trace(x$0: Marker, msg: String): Unit                             = trace(msg)
  def trace(x$0: Marker, msg: String, obj: Object): Unit                = trace(s"$msg, $obj")
  def trace(x$0: Marker, msg: String, obj1: Object, obj2: Object): Unit = trace(s"$msg, $obj1, $obj2")
  def trace(x$0: Marker, msg: String, objs: Array[? <: Object]): Unit   = trace(s"$msg ${objs.mkString(", ")}")
  def trace(x$0: Marker, msg: String, throwable: Throwable): Unit       = trace(throwableMessage(msg, throwable))

  def warn(msg: String, obj: Object): Unit                             = warn(s"$msg, $obj")
  def warn(msg: String, obj1: Object, obj2: Object): Unit              = warn(s"$msg, $obj1, $obj2")
  def warn(msg: String, objs: Array[? <: Object]): Unit                = warn(s"$msg ${objs.mkString(", ")}")
  def warn(msg: String, throwable: Throwable): Unit                    = warn(throwableMessage(msg, throwable))
  def warn(x$0: Marker, msg: String): Unit                             = warn(msg)
  def warn(x$0: Marker, msg: String, obj: Object): Unit                = warn(s"$msg, $obj")
  def warn(x$0: Marker, msg: String, obj1: Object, obj2: Object): Unit = warn(s"$msg, $obj1, $obj2")
  def warn(x$0: Marker, msg: String, objs: Array[? <: Object]): Unit   = warn(s"$msg ${objs.mkString(", ")}")
  def warn(x$0: Marker, msg: String, throwable: Throwable): Unit       = warn(throwableMessage(msg, throwable))

  private def testLevel(logLevel: LogLevel): Boolean = true

  def isDebugEnabled(): Boolean = testLevel(LogLevel.Debug)
  def isErrorEnabled(): Boolean = testLevel(LogLevel.Error)
  def isInfoEnabled(): Boolean  = testLevel(LogLevel.Info)
  def isTraceEnabled(): Boolean = testLevel(LogLevel.Trace)
  def isWarnEnabled(): Boolean  = testLevel(LogLevel.Warn)

  def isInfoEnabled(x$0: Marker): Boolean  = isInfoEnabled()
  def isTraceEnabled(x$0: Marker): Boolean = isTraceEnabled()
  def isDebugEnabled(x$0: Marker): Boolean = isDebugEnabled()
  def isErrorEnabled(x$0: Marker): Boolean = isErrorEnabled()
  def isWarnEnabled(x$0: Marker): Boolean  = isWarnEnabled()

end Slf4jWoofLoggerImpl
