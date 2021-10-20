# Clog

A pure Scala logging library with no reflection


## Highlights

* Pure _Scala_ library
* Made with _Cats Effect_
* Macro based (_no runtime reflection_)
  * Can be built for _scala.js_ in the future!
* No logback
  * Configured with plain Scala code

## Example 

```scala
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import clog.*

val consoleOutput: Output[IO] = new Output[IO]:
  def output(str: String)      = IO.delay(println(str))
  def outputError(str: String) = output(str) // MDOC ignores stderr

given Printer = NoColorPrinter()

val ioLogger: IO[Logger[IO]] = Logger.makeIoLogger(consoleOutput)

def program(using Logger[IO]): IO[Unit] = 
  for
    _ <- Logger[IO].debug("This is some debug")
    _ <- Logger[IO].info("HEY!")
    _ <- Logger[IO].warn("I'm warning you")
    _ <- Logger[IO].error("I give up")
  yield ()

val main: IO[Unit] = 
  for
    given Logger[IO]  <- ioLogger
    _                 <- program
  yield
    ()
```

and running it yields:

```scala
import cats.effect.unsafe.implicits.global
main.unsafeRunSync()
// 08:57:56 [DEBUG] repl.MdocSession$.App: This is some debug (.:34)
// 08:57:56 [INFO ] repl.MdocSession$.App: HEY! (.:35)
// 08:57:56 [WARN ] repl.MdocSession$.App: I'm warning you (.:36)
// 08:57:56 [ERROR] repl.MdocSession$.App: I give up (.:37)
```


We can also re-use the program and add context to our logger:

```scala
import Logger.*
val mainWithContext: IO[Unit] = 
  for
    given Logger[IO]  <- ioLogger
    _                 <- program.withLogContext("trace-id", "4d334544-6462-43fa-b0b1-12846f871573")
  yield ()
```

And running with context yields:

```scala
mainWithContext.unsafeRunSync()
// 08:57:56 [DEBUG] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: This is some debug (.:34)
// 08:57:56 [INFO ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: HEY! (.:35)
// 08:57:56 [WARN ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I'm warning you (.:36)
// 08:57:56 [ERROR] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I give up (.:37)
```