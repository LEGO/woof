package org.legogroup.woof

import cats.Monad
import cats.effect.IO
import cats.effect.kernel.Clock
import org.legogroup.woof.Logger.{ioStringLocal, StringLocal}
import cats.syntax.all.*

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
    if shouldLog(level, message, filter) then logToOutputs(level, message) else ().pure

  private[woof] def shouldLog(level: LogLevel, message: String, filter: Filter)(using logInfo: LogInfo): Boolean =
    filter match
      case Filter.AtLeastLevel(atLeastLevel) => level >= atLeastLevel
      case Filter.ExactLevel(exactLevel)     => level === exactLevel
      case Filter.ClassRegex(regex)          => regex.matches(logInfo.enclosingClass.printableName)
      case Filter.MessageFilter(filter)      => filter(message)
      case Filter.LineNumberFilter(filter)   => filter(logInfo.lineNumber)
      case Filter.CompositeAnd(a, b)         => shouldLog(level, message, a) && shouldLog(level, message, b)
      case Filter.CompositeOr(a, b)          => shouldLog(level, message, a) || shouldLog(level, message, b)
      case Filter.Nothing                    => false
      case Filter.Everything                 => true

  private[woof] def logToOutputs(level: LogLevel, message: String)(using logInfo: LogInfo): F[Unit] = for
    context <- summon[StringLocal[F]].ask
    logLine <- makeLogString(level, logInfo, message, context)
    _       <- doOutputs(level, logLine)
  yield ()

end DefaultLogger

object DefaultLogger:
  def makeIo(output: Output[IO], outputs: Output[IO]*)(using Clock[IO], Printer, Filter): IO[DefaultLogger[IO]] =
    for given StringLocal[IO] <- ioStringLocal
    yield new DefaultLogger[IO](output, outputs*)
end DefaultLogger
