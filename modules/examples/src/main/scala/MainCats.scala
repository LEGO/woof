import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import clog.*

object MainCats extends IOApp:

  import Logger.given_Printer

  override def run(args: List[String]): IO[ExitCode] =
    for
      logger <- Logger.makeIoLogger(Output.fromConsole)
      _      <- logger.debug("This is some debug")
      _      <- logger.info("HEY!")
      _      <- logger.warn("I'm warning you")
      _      <- logger.error("I give up")
    yield ExitCode.Success

end MainCats
