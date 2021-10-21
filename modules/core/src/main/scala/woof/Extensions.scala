package woof

import cats.Applicative
import cats.FlatMap
import cats.Monad
import cats.effect.Concurrent
import cats.effect.kernel.Clock
import cats.effect.kernel.Temporal
import cats.syntax.all.*
import Logger.LogLevel

import scala.concurrent.duration.FiniteDuration

trait Sleep[F[_]]:
  def sleep(duration: FiniteDuration): F[Unit]
object Sleep:
  def apply[F[_]](using s: Sleep[F]): Sleep[F] = s

extension [F[_]: Concurrent: Clock: Sleep: Logger: Monad, T](f: F[T])
  inline def logConcurrently(
      every: FiniteDuration,
  )(inline log: FiniteDuration => String, level: LogLevel = LogLevel.Debug): F[T] =
    val doLog = repeat[F](every, log.andThen(Logger[F].log(level, _)))
    Concurrent[F]
      .race(doLog, f)
      .flatMap(
        _.fold(_ => new Exception("Log should never terminate on it's own...\"this can't happen\"").raiseError, _.pure),
      )

private def repeat[F[_]: Clock: Sleep: FlatMap](period: FiniteDuration, task: FiniteDuration => F[Unit]): F[Unit] =
  for
    start <- Clock[F].realTime
    _     <- repeatAtFixedRate(period, task, start)
  yield ()

private def repeatAtFixedRate[F[_]: Clock: Sleep: FlatMap](
    period: FiniteDuration,
    task: FiniteDuration => F[Unit],
    startedAt: FiniteDuration,
): F[Unit] =
  Clock[F].realTime.flatMap { start =>
    for
      now    <- Clock[F].realTime
      _      <- task(now - startedAt)
      finish <- Clock[F].realTime
      _      <- Sleep[F].sleep(period - (finish - start))
      _      <- repeatAtFixedRate(period, task, startedAt)
    yield ()
  }
