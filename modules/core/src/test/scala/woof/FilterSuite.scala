package woof

import munit.FunSuite
import munit.CatsEffectSuite
import cats.effect.IO
import java.time.ZoneId
import cats.instances.string
import scala.concurrent.duration.*
import cats.effect.Clock
import cats.Applicative
import cats.syntax.all.*
import Logger.*
import Filter.given_Monoid_Filter
import cats.kernel.Monoid
import java.time.Instant
import java.time.format.DateTimeFormatter
class FilterSuite extends CatsEffectSuite:

  given Printer = NoColorPrinter(testFormatTime)

  val startTime = 549459420.seconds

  val constantClock: Clock[IO] = new Clock[IO]:
    def applicative = Applicative[IO]
    def monotonic   = startTime.pure
    def realTime    = startTime.pure

  def testProgram(using Logger[IO]): IO[Unit] =
    for
      _ <- Logger[IO].info("Info message")
      _ <- Logger[IO].debug("Debug message")
      _ <- Logger[IO].warn("Warning message")
      _ <- Logger[IO].error("Error message")
    yield ()

  test("filter based on levels") {
    val expected = """13:37:00 [WARN ] woof.FilterSuite: Warning message (FilterSuite.scala:32)
13:37:00 [ERROR] woof.FilterSuite: Error message (FilterSuite.scala:33)
"""
    given Filter = Filter.atLeastLevel(LogLevel.Warn)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- Logger.makeIoLogger(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str, expected)
  }

  test("filter should enable class via regex") {
    given Filter = Filter.regexFilter("woof\\.Filter.*".r)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- Logger.makeIoLogger(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str.count(_ == '\n'), 4)
  }

  test("filter should disable class via regex") {
    given Filter = Filter.regexFilter("woof\\.NotFilter.*".r)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- Logger.makeIoLogger(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str.count(_ == '\n'), 0)
  }

  test("filter exact log levels") {
    given Filter = Filter.exactLevel(LogLevel.Info) or Filter.exactLevel(LogLevel.Warn)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- Logger.makeIoLogger(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str.count(_ == '\n'), 2)
  }

end FilterSuite
