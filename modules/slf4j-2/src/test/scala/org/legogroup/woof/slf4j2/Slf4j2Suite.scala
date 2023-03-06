package org.legogroup.woof.slf4j2

import cats.Id
import cats.effect.IO
import cats.effect.kernel.Clock
import org.legogroup.woof.*
import org.slf4j.LoggerFactory

import scala.concurrent.duration.*

class Slf4j2Suite extends munit.CatsEffectSuite:
  override def munitIOTimeout = 10.minutes

  test("should log stuff") {
    given Printer = NoColorPrinter(testFormatTime)

    given Filter = Filter.everything

    given Clock[IO] = leetClock

    for
      stringOutput <- newStringWriter
      woofLogger   <- DefaultLogger.makeIo(stringOutput)
      _            <- woofLogger.registerSlf4j
      slf4jLogger  <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _            <- IO.delay(slf4jLogger.info("HELLO, SLF4J-2!"))
      result       <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.slf4j2.Slf4j2Suite: HELLO, SLF4J-2! (Slf4j2Suite.scala:26)\n",
    )
    end for
  }

  test("should log arrays of objects") {
    given Printer = NoColorPrinter(testFormatTime)

    given Filter = Filter.everything

    given Clock[IO] = leetClock

    for
      stringOutput <- newStringWriter
      woofLogger <- DefaultLogger.makeIo(stringOutput)
      _ <- woofLogger.registerSlf4j
      slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _ <- IO.delay(slf4jLogger.info("HELLO, ARRAYS!", 1, Some(42), List(1337)))
      result <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.slf4j2.Slf4j2Suite: HELLO, ARRAYS! 1, Some(42), List(1337) (Slf4j2Suite.scala:47)\n",
    )
    end for
  }

  test("should respect log levels") {
    given Printer = NoColorPrinter(testFormatTime)

    given Filter = Filter.exactLevel(LogLevel.Warn)

    given Clock[IO] = leetClock

    for
      stringWriter <- newStringWriter
      woofLogger <- DefaultLogger.makeIo(stringWriter)
      _ <- woofLogger.registerSlf4j
      slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _ <- IO.delay(slf4jLogger.info("INFO, SLF4J!"))
      _ <- IO.delay(slf4jLogger.debug("DEBUG, SLF4J!"))
      _ <- IO.delay(slf4jLogger.warn("WARN, SLF4J!"))
      _ <- IO.delay(slf4jLogger.error("ERROR, SLF4J!"))
      result <- stringWriter.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [WARN ] org.legogroup.woof.slf4j2.Slf4j2Suite: WARN, SLF4J! (Slf4j2Suite.scala:70)\n",
    )
    end for
  }

  test("should not fail on null throwable") {
    given Printer = NoColorPrinter(testFormatTime)

    given Filter = Filter.everything

    given Clock[IO] = leetClock

    for
      stringWriter <- newStringWriter
      woofLogger <- DefaultLogger.makeIo(stringWriter)
      _ <- woofLogger.registerSlf4j
      slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _ <- IO.delay(slf4jLogger.debug("null exception", null))
      result <- stringWriter.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [DEBUG] org.legogroup.woof.slf4j2.Slf4j2Suite: null exception  (Slf4j2Suite.scala:92)\n",
    )
    end for
  }

end Slf4j2Suite