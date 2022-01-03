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
class EnclosingClassSuite extends CatsEffectSuite:

  class ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation:

    def testProgram(using Logger[IO]): IO[Unit] =
      for
        _ <- Logger[IO].info("Info message")
        _ <- Logger[IO].debug("Debug message")
        _ <- Logger[IO].warn("Warning message")
        _ <- Logger[IO].error("Error message")
      yield ()
  end ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation

  given Filter = Filter.everything
  given Printer = NoColorPrinter(testFormatTime)

  test("class names abbreviated when too long") {
    val expected = "o.l.w.E.ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation"
    val testClass = new ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)(using leetClock)
      _                <- testClass.testProgram
      str              <- stringWriter.get
    yield assert(str.contains(expected))
  }

  test("terminates when package depth is really deep") {
    assertEquals(EnclosingClass("never.gonna.give.you.up", 1).printableName, "n.g.g.y.up")
  }

  test("does not abbreviate package if too short") {
    assertEquals(EnclosingClass("Too.$hort", 10).printableName, "Too.$hort")
  }

end EnclosingClassSuite

