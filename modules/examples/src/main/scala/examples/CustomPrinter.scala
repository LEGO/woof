package examples

import cats.effect.{IO, IOApp}
import org.legogroup.woof.{*, given}

object CustomPrinter extends IOApp.Simple:

  given Filter = Filter.everything
  given Printer with
    def toPrint(
        epochMillis: EpochMillis,
        level: LogLevel,
        info: LogInfo,
        message: String,
        context: List[(String, String)],
    ): String = s"LEVEL:${level.ordinal}, $message -- at epoch ${epochMillis.millis}"

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- Logger[IO].info("Hello, custom printer!")
      _                <- Logger[IO].info("Hello again, custom printer!")
      _                <- Logger[IO].info("Goodbye, custom printer!")
    yield ()
end CustomPrinter
