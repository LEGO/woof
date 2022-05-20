package examples.filters

import org.legogroup.woof.{*, given}
import cats.syntax.all.*
import cats.Monad
import cats.effect.{IO, IOApp}
import scala.util.matching.Regex

class RegexFilter[F[_]: Logger: Monad]:

  def run(): F[Unit] =
    for
      _ <- Logger[F].trace("This is [TRACE]")
      _ <- Logger[F].debug("This is [DEBUG]")
    yield ()
  end run

end RegexFilter

class AnotherProgram[F[_]: Logger: Monad]:

  def run(): F[Unit] =
    for
      _ <- Logger[F].trace("This is [TRACE]")
      _ <- Logger[F].debug("This is [DEBUG]")
    yield ()
  end run

end AnotherProgram

object RegexFilter extends IOApp.Simple:

  given Filter  = Filter.regexFilter(".*.RegexFilter.*".r)
  given Printer = ColorPrinter()

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- RegexFilter[IO].run()
      _                <- AnotherProgram[IO].run() // logs from AnotherProgram won't be shown here
    yield ()

end RegexFilter
