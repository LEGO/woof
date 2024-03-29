package examples.filters

import org.legogroup.woof.{*, given}
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

object CustomFilter extends IOApp.Simple:

  given Printer = ColorPrinter()

  def program(using Logger[IO]): IO[Unit] =
    for
      _ <- Logger[IO].info("Serious")
      _ <- Logger[IO].info("business")
      _ <- Logger[IO].info("logs")
      _ <- Logger[IO].info("are")
      _ <- Logger[IO].info("crazy")
      _ <- Logger[IO].info("important")
    yield ()

  def evenLines: IO[Unit] =
    given Filter = Filter.lineNumberFilter(_ % 2 == 0) // only print *even* lines
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- program
    yield ()

  def filterWords: IO[Unit] =
    val filterWords = Set("Serious", "business", "crazy")
    given Filter    = Filter.messageFilter(_.split("\\s").exists(filterWords.contains))
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- program
    yield ()

  def filterNotWords: IO[Unit] =
    val filterWords = Set("crazy")
    given Filter    = Filter.messageFilter(!_.split("\\s").exists(filterWords))
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- program
    yield ()

  def run = List(evenLines, filterWords, filterNotWords).traverse_(_ *> IO.delay(println()))

end CustomFilter
