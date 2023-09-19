package org.legogroup.woof

import cats.effect.kernel.{Clock, Ref}
import cats.effect.std.Console
import cats.effect.{IO, Temporal}
import cats.syntax.all.*
import cats.{Applicative, Show}
import munit.CatsEffectSuite
import org.legogroup.woof.ColorPrinter.Theme
import org.legogroup.woof.Logger.*
import org.legogroup.woof.local.Local

import java.time.ZoneId
import scala.concurrent.duration.*
import scala.jdk.CollectionConverters.*

class LoggerSuite extends CatsEffectSuite:

  given Filter = Filter.everything

  def clockOf(ref: Ref[IO, FiniteDuration]): Clock[IO] = new Clock[IO]:
    def applicative: Applicative[IO]  = Applicative[IO]
    def monotonic: IO[FiniteDuration] = ref.get
    def realTime: IO[FiniteDuration]  = ref.get

  val constantClock: Clock[IO] = new Clock[IO]:
    def applicative: Applicative[IO]  = Applicative[IO]
    def monotonic: IO[FiniteDuration] = startTime.pure
    def realTime: IO[FiniteDuration]  = startTime.pure

  test("log should make log line") {

    given Clock[IO] = constantClock
    given Printer   = NoColorPrinter(testFormatTime)

    val message  = "log message"
    val logInfo  = summon[LogInfo]
    val expected = "1987-05-31 13:37:00 [WARN ] org.legogroup.woof.LoggerSuite: log message (LoggerSuite.scala:37)"

    for
      given StringLocal[IO] <- ioStringLocal
      logger                <- new DefaultLogger[IO](Output.fromConsole).pure[IO]
      line                  <- logger.makeLogString(LogLevel.Warn, logInfo, message, Nil)
    yield assertEquals(line, expected)
  }

  test("log should log in colors") {

    given Clock[IO]   = constantClock
    val theme         = Theme.defaultTheme
    given Printer     = ColorPrinter(theme, formatTime = testFormatTime)
    val reset         = Theme.Style.Reset
    val postfixFormat = theme.postfixFormat
    // format: off
    val expected = s"""1987-05-31 13:37:00 ${theme.levelFormat(LogLevel.Warn)}[WARN ]$reset ${postfixFormat}org.legogroup.woof.LoggerSuite$reset: This is a warning $postfixFormat(LoggerSuite.scala:62)$reset
"""
    // format: on
    for
      strRef      <- Ref[IO].of("")
      stringLocal <- Local.makeIoLocal[List[(String, String)]]
      logger = new DefaultLogger[IO](StringWriter(strRef))(using stringLocal)
      _      <- logger.warn("This is a warning")
      output <- strRef.get
    yield assertEquals(output, expected)
    end for
  }

  test("log concurrently") {

    given Printer   = NoColorPrinter(testFormatTime)
    given Sleep[IO] = IO.sleep
    val program = for
      ref         <- Ref[IO].of("")
      stringLocal <- Local.makeIoLocal[List[(String, String)]]
      given Logger[IO] = new DefaultLogger[IO](StringWriter(ref))(using stringLocal)
      _ <- Sleep[IO]
        .sleep(999.millis)
        .logConcurrently(200.milliseconds)(d => s"${d.toMillis} elapsed")
      logs <- ref.get
    yield assertEquals(
      logs.split("\n").toList,
      List(0, 200, 400, 600, 800)
        .map(t => s"1987-05-31 13:37:00 [DEBUG] org.legogroup.woof.LoggerSuite: $t elapsed (LoggerSuite.scala:78)")
    )

    executeWithStartTime(program)
  }

  test("Should use local context") {
    given Clock[IO] = constantClock
    given Printer   = NoColorPrinter(testFormatTime)

    val message = "log message"
    val logInfo = summon[LogInfo]

    def programLogic(using Logger[IO]) = Logger[IO].info("some info")

    // format: off
    val expected = """1987-05-31 13:37:00 [INFO ] correlation-id=21c78595-ef21-4df0-987e-8af6aab6f346, locale=da-DK org.legogroup.woof.LoggerSuite: some info (LoggerSuite.scala:96)
1987-05-31 13:37:00 [INFO ] org.legogroup.woof.LoggerSuite: some info (LoggerSuite.scala:96)
"""
    // format: on
    for
      given StringLocal[IO] <- Local.makeIoLocal[List[(String, String)]]
      strRef                <- Ref[IO].of("")
      given Logger[IO] = new DefaultLogger[IO](StringWriter(strRef))
      _ <- programLogic
        .withLogContext("locale", "da-DK")
        .withLogContext("correlation-id", "21c78595-ef21-4df0-987e-8af6aab6f346")
      _      <- programLogic
      output <- strRef.get
    yield assertEquals(output, expected)
    end for
  }

  test("Add var-args context") {
    given Clock[IO] = constantClock

    given Printer = NoColorPrinter(testFormatTime)

    def programLogic(using Logger[IO]) = Logger[IO].info("some info")

    // format: off
    val expected =
      """1987-05-31 13:37:00 [INFO ] foo=bar, baz=1337 org.legogroup.woof.LoggerSuite: some info (LoggerSuite.scala:121)
1987-05-31 13:37:00 [INFO ] org.legogroup.woof.LoggerSuite: some info (LoggerSuite.scala:121)
"""
    // format: on
    for
      given StringLocal[IO] <- Local.makeIoLocal[List[(String, String)]]
      strRef                <- Ref[IO].of("")
      given Logger[IO] = new DefaultLogger[IO](StringWriter(strRef))
      _      <- programLogic.withLogContext(keyValuePairs = "foo" -> "bar", "baz" -> "1337")
      _      <- programLogic
      output <- strRef.get
    yield assertEquals(output, expected)
  }
end LoggerSuite
