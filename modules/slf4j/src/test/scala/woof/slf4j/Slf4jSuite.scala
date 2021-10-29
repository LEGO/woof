import org.slf4j.LoggerFactory
import woof.Logger
import cats.Id
import woof.*
import cats.effect.IO
import org.slf4j.impl.StaticLoggerBinder
import woof.slf4j.WoofLogger
class Slf4jSuite extends munit.CatsEffectSuite:

  test("should log stuff") {
    given Printer = ColorPrinter()
    given Filter  = Filter.everything
    for
      logger      <- Logger.makeIoLogger(Output.fromConsole)
      _           <- IO.delay(WoofLogger.logger = Some(logger))
      slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
      _           <- IO.delay(slf4jLogger.info("HELLO, SLF4J!"))
    yield ()
  }

end Slf4jSuite
