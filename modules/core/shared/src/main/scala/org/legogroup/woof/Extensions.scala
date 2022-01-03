package org.legogroup.woof

import cats.effect.Concurrent
import cats.effect.kernel.{Clock, Temporal}
import cats.syntax.all.*
import cats.{Applicative, FlatMap, Monad}

import scala.concurrent.duration.FiniteDuration

@FunctionalInterface
trait Sleep[F[_]]:
  def sleep(duration: FiniteDuration): F[Unit]
object Sleep:
  def apply[F[_]](using s: Sleep[F]): Sleep[F] = s

extension [F[_]: Concurrent: Clock: Sleep: Logger: Monad, T](f: F[T])
  inline def logConcurrently(
      every: FiniteDuration,
  )(inline log: FiniteDuration => String, level: LogLevel = LogLevel.Debug)(using LogInfo): F[T] =
    val doLog = repeat[F](every, log.andThen(Logger[F].doLog(level, _)))
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
