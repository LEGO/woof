package clog.local

import cats.effect.IO
import cats.effect.IOLocal
import cats.kernel.Monoid
import cats.effect.MonadCancel
import cats.syntax.all.*
import cats.effect.MonadCancel

trait Local[F[_]: ([G[_]] =>> MonadCancel[G, Throwable]), T]:
  def ask: F[T]
  def local[A](fa: F[A])(f: T => T): F[A]
  def scope[A](fa: F[A])(e: T): F[A] = local(fa)(_ => e)
end Local

object Local:

  def makeIoLocal[T: Monoid]: IO[Local[IO, T]] = IOLocal(Monoid[T].empty).map(fromIoLocal)

  def fromIoLocal[T](ioLocal: IOLocal[T]): Local[IO, T] = new Local[IO, T]:
    def ask: IO[T] = ioLocal.get
    def local[A](fa: IO[A])(f: T => T): IO[A] =
      for
        before <- ioLocal.get
        updated = f(before)
        a <- ioLocal.set(updated).as(updated).bracket(_ => fa)(_ => ioLocal.set(before))
      yield a

end Local

extension [T: Monoid, U, F[_]: ([G[_]] =>> Local[G, T])](fu: F[U])
  def withAddedContext(t: T) = summon[Local[F, T]].local(fu)(tt => Monoid[T].combine(tt, t))