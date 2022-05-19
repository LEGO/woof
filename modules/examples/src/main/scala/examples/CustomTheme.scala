package examples

import cats.effect.{IO, IOApp}
import org.legogroup.woof.{*, given}
import ColorPrinter.Theme
import ColorPrinter.Theme.*

object CustomTheme extends IOApp.Simple:

  given Filter  = Filter.everything
  given Printer = ColorPrinter(customTheme)

  private def customTheme: Theme = Theme(
    {
      case LogLevel.Info  => Foreground.Black.withBackground(Background.Green)
      case LogLevel.Warn  => Foreground.Black.withBackground(Background.Yellow)
      case LogLevel.Trace => Foreground.Black.withBackground(Background.White).withStyle(Style.Underlined)
      case LogLevel.Error => Foreground.Black.withBackground(Background.Red).withStyle(Style.Bold)
      case level          => defaultTheme.levelFormat(level)
    },
    Foreground.Cyan.withStyle(Style.Underlined),
    Style.Reset,
    Empty,
    Empty,
  )

  def run: IO[Unit] =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- Logger[IO].info("just some info")
      _                <- Logger[IO].debug("incoming debug")
      _                <- Logger[IO].warn("warning ahead")
      _                <- Logger[IO].trace("tracing")
      _                <- Logger[IO].error("something went wrong")
    yield ()

end CustomTheme
