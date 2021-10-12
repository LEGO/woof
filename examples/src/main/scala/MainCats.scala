import cats.effect.kernel.Sync
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.std.Console
import clog.*

object MainCats extends IOApp:

  import Logger.given_Printer
  val logger = new Logger[IO](Output.fromConsole)

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- logger.debug("This is some debug")
      _ <- logger.info("HEY!")
      _ <- logger.warn("I'm warning you")
      _ <- logger.error("I give up")
    yield ExitCode.Success

end MainCats
