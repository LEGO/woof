package org.legogroup.woof.log4cats

import cats.Applicative
import org.legogroup.woof
import org.legogroup.woof.Logger.withLogContext
import org.typelevel.log4cats

object WoofFactory:

  def apply[F[_]: Applicative](logger: woof.Logger[F]): log4cats.LoggerFactory[F] =
    new WoofFactory[F](logger)

end WoofFactory

private class WoofFactory[F[_]: Applicative](logger: woof.Logger[F]) extends log4cats.LoggerFactory[F]:

  override def getLoggerFromName(name: String): log4cats.SelfAwareStructuredLogger[F] =
    new WoofLog4CatsLogger[F](logger, name)

  override def fromName(name: String): F[log4cats.SelfAwareStructuredLogger[F]] =
    Applicative[F].pure(getLoggerFromName(name))

end WoofFactory

private class WoofLog4CatsLogger[F[_]: Applicative](logger: woof.Logger[F], name: String)
    extends log4cats.SelfAwareStructuredLogger[F]:

  private def logInfo(): woof.LogInfo =
    val stacktraceElements = (new Throwable).getStackTrace()
    val lastIndex          = stacktraceElements.reverse.indexWhere(s => s.getClassName == this.getClass.getName)
    val callingMethodIndex = stacktraceElements.size - lastIndex
    val callingMethod      = stacktraceElements(callingMethodIndex)
    val fileName =
      (callingMethod.getClassName.replace('.', '/') + ".scala").split("\\/").takeRight(1).mkString
    val lineNumber = callingMethod.getLineNumber - 1
    woof.LogInfo(woof.EnclosingClass(name), fileName, lineNumber)
  end logInfo

  private def thrMsg(m: String, t: Throwable): String =
    (try s"$m ${t.getMessage}"
    catch case _ => s"$m ")

  override def error(ctx: Map[String, String], t: Throwable)(m: => String): F[Unit] =
    logger.error(thrMsg(m, t))(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def warn(ctx: Map[String, String], t: Throwable)(m: => String): F[Unit] =
    logger.warn(thrMsg(m, t))(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def info(ctx: Map[String, String], t: Throwable)(m: => String): F[Unit] =
    logger.info(thrMsg(m, t))(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def debug(ctx: Map[String, String], t: Throwable)(m: => String): F[Unit] =
    logger.debug(thrMsg(m, t))(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def trace(ctx: Map[String, String], t: Throwable)(m: => String): F[Unit] =
    logger.trace(thrMsg(m, t))(using logInfo()).withLogContext(ctx.toList*)(using logger)

  override def error(t: Throwable)(m: => String): F[Unit] = logger.error(thrMsg(m, t))(using logInfo())
  override def warn(t: Throwable)(m: => String): F[Unit]  = logger.warn(thrMsg(m, t))(using logInfo())
  override def info(t: Throwable)(m: => String): F[Unit]  = logger.info(thrMsg(m, t))(using logInfo())
  override def debug(t: Throwable)(m: => String): F[Unit] = logger.debug(thrMsg(m, t))(using logInfo())
  override def trace(t: Throwable)(m: => String): F[Unit] = logger.trace(thrMsg(m, t))(using logInfo())

  override def error(m: => String): F[Unit] = logger.error(m)(using logInfo())
  override def warn(m: => String): F[Unit]  = logger.warn(m)(using logInfo())
  override def info(m: => String): F[Unit]  = logger.info(m)(using logInfo())
  override def debug(m: => String): F[Unit] = logger.debug(m)(using logInfo())
  override def trace(m: => String): F[Unit] = logger.trace(m)(using logInfo())

  override def isErrorEnabled: F[Boolean] = Applicative[F].pure(true)
  override def isWarnEnabled: F[Boolean]  = Applicative[F].pure(true)
  override def isInfoEnabled: F[Boolean]  = Applicative[F].pure(true)
  override def isDebugEnabled: F[Boolean] = Applicative[F].pure(true)
  override def isTraceEnabled: F[Boolean] = Applicative[F].pure(true)

  override def error(ctx: Map[String, String])(m: => String): F[Unit] =
    logger.error(m)(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def warn(ctx: Map[String, String])(m: => String): F[Unit] =
    logger.warn(m)(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def info(ctx: Map[String, String])(m: => String): F[Unit] =
    logger.info(m)(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def debug(ctx: Map[String, String])(m: => String): F[Unit] =
    logger.debug(m)(using logInfo()).withLogContext(ctx.toList*)(using logger)
  override def trace(ctx: Map[String, String])(m: => String): F[Unit] =
    logger.trace(m)(using logInfo()).withLogContext(ctx.toList*)(using logger)

end WoofLog4CatsLogger
