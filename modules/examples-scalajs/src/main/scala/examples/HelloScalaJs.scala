package examples

import cats.effect.{IO, IOApp}
import org.legogroup.woof.{*, given}

/** To run this, open sbt and call: `examplesJs/fastOptJS` Then, open `modules/examples-scalajs/index.html` in your
  * browser and look at the console
  */
object HelloScalaJs extends IOApp.Simple:

  given Filter  = Filter.everything
  given Printer = NoColorPrinter()

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- Logger[IO].info("Hello, Scala.Js!")
    yield ()

end HelloScalaJs
