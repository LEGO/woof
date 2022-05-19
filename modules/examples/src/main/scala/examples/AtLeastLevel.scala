package examples

import org.legogroup.woof.{*, given}
import cats.syntax.all.*
import cats.Monad
import cats.effect.{IO, IOApp}

class AtLeastLevel[F[_]: Logger: Monad]:

  def run(): F[Unit] =
    for
      _ <- Logger[F].trace("This is [TRACE](this should not be shown.)")
      _ <- Logger[F].debug("This is [DEBUG](this should not be shown.)")
      _ <- Logger[F].info("This is [INFO]")
      _ <- Logger[F].warn("This is [WARN]")
      _ <- Logger[F].error("This is [ERROR]")
    yield ()
  end run

end AtLeastLevel

object AtLeastLevel extends IOApp.Simple:

  given Filter  = Filter.atLeastLevel(LogLevel.Info)
  given Printer = ColorPrinter()

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- AtLeastLevel[IO].run()
    yield ()

end AtLeastLevel
