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
  end ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation

  given Filter = Filter.everything
  given Printer = NoColorPrinter(testFormatTime)

  test("class names abbreviated when too long") {
    val expected = "o.l.w.E.ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation"
    val testClass = new ThisClassNameIsReallyLongAndWillTriggerParentPackageAbbreviation
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- Logger.makeIoLogger(stringWriter)(using testClass.constantClock)
      _                <- testClass.testProgram
      str              <- stringWriter.get
    yield assert(str.contains(expected))
  }

end EnclosingClassSuite

