package examples

import cats.effect.{IO, IOApp, Resource}
import java.io.{PrintWriter, FileWriter}
import org.legogroup.woof.{*, given}

/*
* Run the program and inspect the files `woof.err`,
* and `woof.log` in the root of the project.
*/
object FileOutput extends IOApp.Simple:

  given Filter  = Filter.everything
  given Printer = NoColorPrinter()

  val fileOutput =
    def writeLine(line: String, logPath: String) =
      val writer = IO(PrintWriter(FileWriter(logPath, true)))
      val res = Resource.make(writer) { w => IO(w.close) }
      res.use { w => IO(w.println(line)) }
    new Output[IO]:
      def output(str: String)      = writeLine(str, "woof.log")
      def outputError(str: String) = writeLine(str, "woof.err")

  def run =
    for
      given Logger[IO] <- DefaultLogger.makeIo(fileOutput, Output.fromConsole)
      _                <- Logger[IO].info("Hello, Info!")
      _                <- Logger[IO].error("Hello, Error!")
    yield ()

end FileOutput