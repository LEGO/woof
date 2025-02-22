package org.legogroup.woof.log4cats

import cats.effect.IO
import cats.effect.kernel.Clock
import cats.effect.std.Dispatcher
import cats.Id
import org.legogroup.woof.*
import org.typelevel.log4cats.LoggerFactory
import scala.concurrent.duration.*

class Log4CatsSuite extends munit.CatsEffectSuite:

  override def munitIOTimeout = 10.minutes

  private val ctx = Map("a" -> "a", "my context" -> "MY CONTEXT")

  test("should log stuff") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock

    for
      stringOutput   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringOutput)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.info("HELLO, Log4Cats!")
      result         <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.log4cats.Log4CatsSuite: HELLO, Log4Cats! (Log4CatsSuite.scala:26)\n",
    )
    end for
  }

  test("should respect log levels") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.exactLevel(LogLevel.Warn)
    given Clock[IO] = leetClock

    for
      stringWriter   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringWriter)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.error("ERROR, Log4Cats!")
      _              <- log4catsLogger.warn("WARN, Log4Cats!")
      _              <- log4catsLogger.info("INFO, Log4Cats!")
      _              <- log4catsLogger.debug("DEBUG, Log4Cats!")
      _              <- log4catsLogger.debug("TRACE, Log4Cats!")
      result         <- stringWriter.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [WARN ] org.legogroup.woof.log4cats.Log4CatsSuite: WARN, Log4Cats! (Log4CatsSuite.scala:45)\n",
    )
    end for
  }

  test("should log context") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock

    for
      stringOutput   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringOutput)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.info(Map("a" -> "a", "my context" -> "MY CONTEXT"))("HELLO, CONTEXT!")
      result         <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] a=a, my context=MY CONTEXT org.legogroup.woof.log4cats.Log4CatsSuite: HELLO, CONTEXT! (Log4CatsSuite.scala:66)\n",
    )
    end for
  }

  test("should log throwable") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock

    for
      stringOutput   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringOutput)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.info(new RuntimeException("BOOM!"))("THROWABLE")
      result         <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.log4cats.Log4CatsSuite: THROWABLE BOOM! (Log4CatsSuite.scala:84)\n",
    )
    end for
  }

  test("should log context and throwable") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock

    for
      stringOutput   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringOutput)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.info(ctx, new RuntimeException("BOOM!"))("CONTEXT + THROWABLE")
      result         <- stringOutput.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [INFO ] a=a, my context=MY CONTEXT org.legogroup.woof.log4cats.Log4CatsSuite: CONTEXT + THROWABLE BOOM! (Log4CatsSuite.scala:102)\n",
    )
    end for
  }

  test("should not fail on null throwable") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock

    for
      stringWriter   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringWriter)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.debug(null: Throwable)("NULL THROWABLE")
      result         <- stringWriter.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [DEBUG] org.legogroup.woof.log4cats.Log4CatsSuite: NULL THROWABLE  (Log4CatsSuite.scala:120)\n",
    )
    end for
  }

  test("should not fail on null throwable with context") {
    given Printer   = NoColorPrinter(testFormatTime)
    given Filter    = Filter.everything
    given Clock[IO] = leetClock

    for
      stringWriter   <- newStringWriter
      woofLogger     <- DefaultLogger.makeIo(stringWriter)
      log4catsLogger <- IO(WoofFactory[IO](woofLogger).getLogger)
      _              <- log4catsLogger.debug(ctx, null: Throwable)("NULL THROWABLE + CONTEXT")
      result         <- stringWriter.get
    yield assertEquals(
      result,
      "1987-05-31 13:37:00 [DEBUG] a=a, my context=MY CONTEXT org.legogroup.woof.log4cats.Log4CatsSuite: NULL THROWABLE + CONTEXT  (Log4CatsSuite.scala:138)\n",
    )
    end for
  }

end Log4CatsSuite
