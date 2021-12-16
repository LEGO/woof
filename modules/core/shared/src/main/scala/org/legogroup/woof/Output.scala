package org.legogroup.woof
import cats.effect.std.Console

trait Output[F[_]]:
  def output(str: String): F[Unit]
  def outputError(str: String): F[Unit]
end Output

object Output:
  def fromConsole[F[_]: Console]: Output[F] = new Output[F]:
    def output(str: String): F[Unit]      = Console[F].println(str)
    def outputError(str: String): F[Unit] = Console[F].errorln(str)
end Output
