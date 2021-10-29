package woof.slf4j

import org.slf4j.LoggerFactory
import woof.Logger
import cats.Id
import woof.*
import cats.effect.IO
import org.slf4j.impl.StaticLoggerBinder
import woof.slf4j.WoofLogger
import scala.concurrent.duration.*
import Logger.LogLevel
import cats.effect.kernel.Clock
class Slf4jSuite extends munit.CatsEffectSuite:

  override def munitTimeout = 10.minutes

  test("should log stuff") {
    given Printer   = NoColorPrinter()
    given Filter    = Filter.everything
    given Clock[IO] = leetClock
    for
      stringOutput <- newStringWriter
      woofLogger   <- Logger.makeIoLogger(stringOutput)
      _            <- IO.delay(WoofLogger.logger = Some(woofLogger))
      slf4jLogger  <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _            <- IO.delay(slf4jLogger.info("HELLO, SLF4J!"))
      result       <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] woof.slf4j.Slf4jSuite: HELLO, SLF4J! (Slf4jSuite.scala:26)\n",
    )
    end for
  }

  test("should log arrays of objects") {
    given Printer   = NoColorPrinter()
    given Filter    = Filter.everything
    given Clock[IO] = leetClock
    for
      stringOutput <- newStringWriter
      woofLogger   <- Logger.makeIoLogger(stringOutput)
      _            <- IO.delay(WoofLogger.logger = Some(woofLogger))
      slf4jLogger  <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _            <- IO.delay(slf4jLogger.info("HELLO, ARRAYS!", 1, Some(42), List(1337)))
      result       <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] woof.slf4j.Slf4jSuite: HELLO, ARRAYS! 1, Some(42), List(1337) (Slf4jSuite.scala:44)\n",
    )
    end for
  }

  test("should check log levels") {
    given Printer = ColorPrinter()
    given Filter  = Filter.exactLevel(LogLevel.Warn)
    for
      logger      <- Logger.makeIoLogger(Output.fromConsole)
      _           <- IO.delay(WoofLogger.logger = Some(logger))
      slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _           <- IO.delay(slf4jLogger.info("HELLO, SLF4J!"))
    yield
      assert(slf4jLogger.isWarnEnabled)
      assert(!slf4jLogger.isDebugEnabled)
      assert(!slf4jLogger.isInfoEnabled)
      assert(!slf4jLogger.isDebugEnabled)
      assert(!slf4jLogger.isTraceEnabled)
    end for
  }

end Slf4jSuite
