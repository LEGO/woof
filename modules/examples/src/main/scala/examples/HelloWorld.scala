import cats.effect.{IO, IOApp}
import org.legogroup.woof.{*, given}

object HelloWorld extends IOApp.Simple:

  given Filter  = Filter.everything
  given Printer = ColorPrinter()
  
  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(Output.fromConsole)
      _                <- Logger[IO].info("Hello, World!")
    yield ()
    
end HelloWorld
