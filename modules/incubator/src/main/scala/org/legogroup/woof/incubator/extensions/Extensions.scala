package org.legogroup.woof.incubator.extensions

import org.legogroup.woof.{Logger, repeatAtFixedRate, LogLevel, LogInfo, given}
import cats.{Applicative, Parallel}
import cats.effect.{Concurrent, Ref}
import cats.syntax.all.*
import cats.effect.syntax.all.*
import scala.concurrent.duration.*
import scala.annotation.tailrec
import org.legogroup.woof.Sleep
import cats.syntax.all.*
import cats.Monad
import cats.effect.kernel.Clock
import cats.Show

def repeat[F[_]: Sleep: Monad](every: FiniteDuration, f: F[Unit]): F[Unit] =
  for
    _ <- Sleep[F].sleep(every)
    _ <- f
    _ <- repeat(every, f)
  yield ()

extension [F[_]: Logger: Monad, T](f: F[T])
  private inline def log(msg: T => String, level: LogLevel)(using LogInfo): F[T] =
    f.flatTap(m => Logger[F].doLog(level, msg(m)))

  inline def debug(msg: T => String): F[T] = log(msg, LogLevel.Debug)
  inline def error(msg: T => String): F[T] = log(msg, LogLevel.Error)
  inline def info(msg: T => String): F[T]  = log(msg, LogLevel.Info)
  inline def trace(msg: T => String): F[T] = log(msg, LogLevel.Trace)
  inline def warn(msg: T => String): F[T]  = log(msg, LogLevel.Warn)

extension [F[_]: Logger: Monad, T: Show](f: F[T])
  private inline def logShow(level: LogLevel)(using LogInfo): F[T] =
    f.flatTap(t => Logger[F].doLog(level, Show[T].show(t)))

  inline def debugShow: F[T] = logShow(LogLevel.Debug)
  inline def errorShow: F[T] = logShow(LogLevel.Error)
  inline def infoShow: F[T]  = logShow(LogLevel.Info)
  inline def traceShow: F[T] = logShow(LogLevel.Trace)
  inline def warnShow: F[T]  = logShow(LogLevel.Warn)

extension [A](as: List[A])
  inline def traverseLog[B, G[_]: Logger: Applicative](g: A => G[B], msg: (A, Double) => String): G[List[B]] =
    val n = as.length
    as.zipWithIndex.traverse((a, i) => g(a) <* Logger[G].debug(msg(a, (i + 1).toDouble / n.toDouble * 100d)))

  inline def parTraverseLog[B, G[_]: Parallel: Concurrent: Logger: Applicative](parallelism: Int)(
      g: A => G[B],
      msg: (A, Double) => String,
  ): G[List[B]] =
    val n = as.length
    for
      ref <- Ref[G].of(0)
      result <- as.parTraverseN(parallelism)(a =>
        for
          result  <- g(a)
          updated <- ref.updateAndGet(_ + 1)
          _       <- Logger[G].debug(msg(a, updated.toDouble / n.toDouble * 100d))
        yield result,
      )
    yield result
    end for
  end parTraverseLog

  inline def parTraverseLogProgress[B, G[_]: Clock: Sleep: Parallel: Concurrent: Logger: Applicative](
      parallelism: Int,
      every: FiniteDuration
  )(
      g: A => G[B],
      msg: Double => String,
  ) =
    val n = as.length
    for
      ref <- Ref[G].of(0)
      logfiber <- repeat(
        every,
        ref.get.flatMap(d => Logger[G].debug(msg(d.toDouble / as.length.toDouble * 100d)))
      ).start
      result <- as
        .parTraverseN(parallelism)(a =>
          for
            result <- g(a)
            _      <- ref.update(_ + 1)
          yield result
        )
        .start
      rj <- result.join
      _  <- logfiber.cancel
    yield rj
    end for
  end parTraverseLogProgress

end extension
