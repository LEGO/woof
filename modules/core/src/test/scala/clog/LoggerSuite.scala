package clog

import munit.CatsEffectSuite
import cats.effect.IO
import cats.effect.kernel.Clock
import scala.concurrent.duration.*
import cats.syntax.all.*
import cats.Applicative
import java.time.ZoneId
import collection.JavaConverters.asScalaSetConverter
import cats.effect.kernel.Ref
import cats.effect.std.Console
import cats.Show
import cats.effect.Temporal
import ColorPrinter.Theme
import Logger.*
import clog.local.Local
class LoggerSuite extends CatsEffectSuite:

  class StringWriter(val ref: Ref[IO, String]) extends Output[IO]:
    def output(str: String): IO[Unit]      = ref.update(log => s"$log$str\n")
    def outputError(str: String): IO[Unit] = output(str)

  val startTime = 549459420.seconds

  def testTime(start: FiniteDuration): IO[(Clock[IO], Sleep[IO], Ref[IO, FiniteDuration])] =
    for finitedur <- Ref[IO].of(start)
    yield (clockOf(finitedur), sleepOf(finitedur), finitedur)

  def sleepOf(ref: Ref[IO, FiniteDuration]): Sleep[IO] = new Sleep[IO]:
    def sleep(dur: FiniteDuration): IO[Unit] = ref.update(_ + dur)

  def clockOf(ref: Ref[IO, FiniteDuration]): Clock[IO] = new Clock[IO]:
    def applicative = Applicative[IO]
    def monotonic   = ref.get
    def realTime    = ref.get

  val constantClock: Clock[IO] = new Clock[IO]:
    def applicative = Applicative[IO]
    def monotonic   = startTime.pure
    def realTime    = startTime.pure

  test("log should make log line") {

    given Clock[IO] = constantClock
    given Printer   = NoColorPrinter(zoneId = ZoneId.of("Europe/Copenhagen"))

    val message  = "log message"
    val logInfo  = Logging.info(message)
    val expected = "13:37:00 [WARN ] clog.LoggerSuite: log message (LoggerSuite.scala:49)"

    for
      logger <- Logger.makeIoLogger(Output.fromConsole)
      line   <- logger.makeLogLine(Logger.LogLevel.Warn, logInfo, message, Nil)
    yield assertEquals(line, expected)
  }

  test("log should log in colors") {

    given Clock[IO]   = constantClock
    val theme         = Theme.defaultTheme
    given Printer     = ColorPrinter(theme = theme, zoneId = ZoneId.of("Europe/Copenhagen"))
    val reset         = Theme.Style.Reset
    val postfixFormat = theme.postfixFormat
    // format: off
    val expected = s"""13:37:00 ${theme.levelFormat(LogLevel.Warn)}[WARN ]$reset ${postfixFormat}clog.LoggerSuite$reset: This is a warning $postfixFormat(LoggerSuite.scala:73)$reset
"""
    // format: on
    for
      strRef      <- Ref[IO].of("")
      stringLocal <- Local.makeIoLocal[List[(String, String)]]
      logger = new Logger[IO](StringWriter(strRef))(using stringLocal)
      _      <- logger.warn("This is a warning")
      output <- strRef.get
    yield assertEquals(output.toList, expected.toList)
    end for
  }

  test("log concurrently") {

    for
      ref                      <- Ref[IO].of("")
      (clock, sleep, clockRef) <- testTime(startTime)
      stringLocal              <- Local.makeIoLocal[List[(String, String)]]
      given Sleep[IO] = sleep
      given Clock[IO] = clock
      given Logger[IO] =
        given Clock[IO] = clock
        given Printer   = NoColorPrinter(zoneId = ZoneId.of("Europe/Copenhagen"))
        new Logger[IO](StringWriter(ref))(using stringLocal)
      _ <- clockRef.get
        .iterateUntil(_ >= (startTime + 1.second))
        .logConcurrently(200.milliseconds)(d => s"${d.toMillis} elapsed")
      logs <- ref.get
    yield
      val times = logs
      assert(times.count(_ == '\n') >= 5)
      assertEquals(
        times.split("\n")(2),
        "13:37:00 [DEBUG] clog.LoggerSuite: 400 elapsed (LoggerSuite.scala:91)",
      )
  }

  test("Should use local context") {
    given Clock[IO] = constantClock
    given Printer   = NoColorPrinter(zoneId = ZoneId.of("Europe/Copenhagen"))

    val message = "log message"
    val logInfo = Logging.info(message)

    def programLogic(using Logger[IO]) = Logger[IO].info("some info")

    val context = List(
      "correlation-id" -> "21c78595-ef21-4df0-987e-8af6aab6f346",
      "locale"         -> "da-DK",
    )


    // format: off
    val expected = """13:37:00 [INFO ] correlation-id=21c78595-ef21-4df0-987e-8af6aab6f346, locale=da-DK clog.LoggerSuite: some info (LoggerSuite.scala:111)
13:37:00 [INFO ] clog.LoggerSuite: some info (LoggerSuite.scala:111)
"""
    // format: on
    for
      given StringLocal[IO] <- Local.makeIoLocal[List[(String, String)]]
      strRef                <- Ref[IO].of("")
      given Logger[IO] = new Logger[IO](StringWriter(strRef))
      _ <- programLogic
        .withLogContext("correlation-id", "21c78595-ef21-4df0-987e-8af6aab6f346")
        .withLogContext("locale", "da-DK")
      _      <- programLogic
      output <- strRef.get
    yield assertEquals(output, expected)
    end for
  }
end LoggerSuite
