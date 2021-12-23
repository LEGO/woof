package org.legogroup.woof.slf4j

import cats.Id
import cats.effect.IO
import cats.effect.kernel.Clock
import org.legogroup.woof.*
import org.slf4j.LoggerFactory
import org.slf4j.impl.StaticLoggerBinder

import scala.concurrent.duration.*

class Slf4jSuite extends munit.CatsEffectSuite:

  override def munitTimeout = 10.minutes

  test("should log stuff") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock
    for
      stringOutput <- newStringWriter
      woofLogger   <- Logger.makeIoLogger(stringOutput)
      _            <- woofLogger.registerSlf4j
      slf4jLogger  <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _            <- IO.delay(slf4jLogger.info("HELLO, SLF4J!"))
      result       <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.slf4j.Slf4jSuite: HELLO, SLF4J! (Slf4jSuite.scala:25)\n",
    )
    end for
  }

  test("should log arrays of objects") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock
    for
      stringOutput <- newStringWriter
      woofLogger   <- Logger.makeIoLogger(stringOutput)
      _            <- woofLogger.registerSlf4j
      slf4jLogger  <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _            <- IO.delay(slf4jLogger.info("HELLO, ARRAYS!", 1, Some(42), List(1337)))
      result       <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.slf4j.Slf4jSuite: HELLO, ARRAYS! 1, Some(42), List(1337) (Slf4jSuite.scala:43)\n",
    )
    end for
  }

  test("should respect log levels") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.exactLevel(LogLevel.Warn)
    given Clock[IO] = leetClock
    for
      stringWriter <- newStringWriter
      woofLogger   <- Logger.makeIoLogger(stringWriter)
      _            <- woofLogger.registerSlf4j
      slf4jLogger  <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _            <- IO.delay(slf4jLogger.info("INFO, SLF4J!"))
      _            <- IO.delay(slf4jLogger.debug("DEBUG, SLF4J!"))
      _            <- IO.delay(slf4jLogger.warn("WARN, SLF4J!"))
      _            <- IO.delay(slf4jLogger.error("ERROR, SLF4J!"))
      result       <- stringWriter.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [WARN ] org.legogroup.woof.slf4j.Slf4jSuite: WARN, SLF4J! (Slf4jSuite.scala:63)\n",
    )
    end for
  }

end Slf4jSuite
