package examples

import cats.effect.{IO, IOApp}
import org.legogroup.woof.{*, given}
import cats.syntax.all.*

object LogLevelFromEnv extends IOApp.Simple:

  given Printer = ColorPrinter()

  def run =
    for
      logLevelEnv <- IO.delay(sys.env.getOrElse("LOG_LEVEL", "Warn"))
      logLevel    <- LogLevel.valueOf(logLevelEnv).pure[IO]
      given Filter = Filter.atLeastLevel(logLevel)
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- Logger[IO].trace("🔇 Hello from Trace 🔇")
      _                <- Logger[IO].debug("🔇 Hello from Debug 🔊")
      _                <- Logger[IO].info("🔇 Hello from Info 🔇")
      _                <- Logger[IO].error("🔊 Hello from Error 🔊")
      _                <- Logger[IO].warn("🔊 Hello from Warning 🔊")
    yield ()

end LogLevelFromEnv
