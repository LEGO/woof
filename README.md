# Woof

A pure Scala logging library with no reflection

![](dog-svgrepo-com.svg)

## Highlights

* Pure **Scala 3** library
* Made with _Cats Effect_
* Macro based (_no runtime reflection_)
  * Can be built for _scala.js_ in the future!
* No logback
  * Configured with plain Scala code

## Example 

```scala
import cats.effect.IO
import woof.*

// Main program
def program(using Logger[IO]): IO[Unit] = 
  for
    _ <- Logger[IO].debug("This is some debug")
    _ <- Logger[IO].info("HEY!")
    _ <- Logger[IO].warn("I'm warning you")
    _ <- Logger[IO].error("I give up")
  yield ()

// Setup/configuration of logger
val consoleOutput: Output[IO] = new Output[IO]:
  def output(str: String)      = IO.delay(println(str))
  def outputError(str: String) = output(str) // MDOC ignores stderr

given Filter  = Filter.everything
given Printer = NoColorPrinter()

// Running the program
val main: IO[Unit] = 
  for
    given Logger[IO]  <- Logger.makeIoLogger(consoleOutput)
    _                 <- program
  yield ()
```

and running it yields:

```scala
import cats.effect.unsafe.implicits.global
main.unsafeRunSync()
// 08:14:59 [DEBUG] repl.MdocSession$.App: This is some debug (.:15)
// 08:14:59 [INFO ] repl.MdocSession$.App: HEY! (.:16)
// 08:14:59 [WARN ] repl.MdocSession$.App: I'm warning you (.:17)
// 08:14:59 [ERROR] repl.MdocSession$.App: I give up (.:18)
```


We can also re-use the program and add context to our logger:

```scala
import Logger.*
val mainWithContext: IO[Unit] = 
  for
    given Logger[IO]  <- Logger.makeIoLogger(consoleOutput)
    _                 <- program.withLogContext("trace-id", "4d334544-6462-43fa-b0b1-12846f871573")
  yield ()
```

And running with context yields:

```scala
mainWithContext.unsafeRunSync()
// 08:14:59 [DEBUG] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: This is some debug (.:15)
// 08:14:59 [INFO ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: HEY! (.:16)
// 08:14:59 [WARN ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I'm warning you (.:17)
// 08:14:59 [ERROR] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I give up (.:18)
```