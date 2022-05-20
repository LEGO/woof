package examples.filters

import org.legogroup.woof.{*, given}
import cats.syntax.all.*
import cats.Monad
import cats.effect.{IO, IOApp}

class ExactLevel[F[_]: Logger: Monad]:

  def run(): F[Unit] =
    for
      _ <- Logger[F].trace("This is [TRACE](this should not be shown.)")
      _ <- Logger[F].debug("This is [DEBUG](this should not be shown.)")
      _ <- Logger[F].info("This is [INFO](You should see only this line.)")
      _ <- Logger[F].warn("This is [WARN](this should not be shown.)")
      _ <- Logger[F].error("This is [ERROR](this should not be shown.)")
    yield ()
  end run

end ExactLevel

object ExactLevel extends IOApp.Simple:

  given Filter  = Filter.exactLevel(LogLevel.Info)
  given Printer = ColorPrinter()

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- ExactLevel[IO].run()
    yield ()

end ExactLevel
