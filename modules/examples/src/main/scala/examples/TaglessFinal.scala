package examples

import org.legogroup.woof.{*, given}
import cats.syntax.all.*
import cats.Monad
import cats.effect.{IO, IOApp}

class TaglessFinal[F[_]: Logger: Monad]:

  def run(): F[Unit] =
    for
      _ <- Logger[F].info("This")
      _ <- Logger[F].warn("is")
      _ <- Logger[F].debug("tagless")
      _ <- Logger[F].trace("final")
    yield ()

end TaglessFinal

object TaglessFinal extends IOApp.Simple:

  given Filter  = Filter.everything
  given Printer = ColorPrinter()

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- TaglessFinal[IO].run()
    yield ()

end TaglessFinal
