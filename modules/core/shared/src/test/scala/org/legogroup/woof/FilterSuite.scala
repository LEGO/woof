package org.legogroup.woof

import cats.Applicative
import cats.effect.{Clock, IO}
import cats.instances.string
import cats.kernel.Monoid
import cats.syntax.all.*
import munit.{CatsEffectSuite, FunSuite}
import org.legogroup.woof.Filter.given_Monoid_Filter
import org.legogroup.woof.Logger.*

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import scala.concurrent.duration.*
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
    val expected = """1987-05-31 13:37:00 [WARN ] org.legogroup.woof.FilterSuite: Warning message (FilterSuite.scala:30)
1987-05-31 13:37:00 [ERROR] org.legogroup.woof.FilterSuite: Error message (FilterSuite.scala:31)
"""
    given Filter = Filter.atLeastLevel(LogLevel.Warn)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str, expected)
  }

  test("filter should enable class via regex") {
    given Filter = Filter.regexFilter("org\\.legogroup\\.woof\\.Filter.*".r)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str.count(_ == '\n'), 4)
  }

  test("filter should disable class via regex") {
    given Filter = Filter.regexFilter("org.legogroup.woof\\.NotFilter.*".r)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str.count(_ == '\n'), 0)
  }

  test("filter exact log levels") {
    given Filter = Filter.exactLevel(LogLevel.Info) or Filter.exactLevel(LogLevel.Warn)
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)(using constantClock)
      _                <- testProgram
      str              <- stringWriter.get
    yield assertEquals(str.count(_ == '\n'), 2)
  }

  test("log levels have priorities") {
    import LogLevel.*
    import cats.Order.catsKernelOrderingForOrder
    val ordinalValues: List[(LogLevel, Int)] = LogLevel.values.sorted.toList.fproduct(_.ordinal)
    assertEquals(ordinalValues, List(Trace -> 0, Debug -> 1, Info -> 2, Warn -> 3, Error -> 4))
  }

end FilterSuite
