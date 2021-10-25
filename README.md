# Woof

A **pure** _(in both senses of the word!)_ **Scala 3** logging library with **no runtime reflection**.

![](dog-svgrepo-com.svg)

## Highlights

* Pure **Scala 3** library
* Made with _Cats Effect_
* Macro based (_no runtime reflection_)
  * Can be built for _scala.js_ in the future!
* No slf4j
* Configured with plain Scala code

## Example 

```scala
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import woof.*

val consoleOutput: Output[IO] = new Output[IO]:
  def output(str: String)      = IO.delay(println(str))
  def outputError(str: String) = output(str) // MDOC ignores stderr

given Filter = Filter.everything
given Printer = NoColorPrinter()

def program(using Logger[IO]): IO[Unit] = 
  for
    _ <- Logger[IO].debug("This is some debug")
    _ <- Logger[IO].info("HEY!")
    _ <- Logger[IO].warn("I'm warning you")
    _ <- Logger[IO].error("I give up")
  yield ()

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
// 2021-10-25 14:08:51 [DEBUG] repl.MdocSession$.App: This is some debug (.:33)
// 2021-10-25 14:08:52 [INFO ] repl.MdocSession$.App: HEY! (.:34)
// 2021-10-25 14:08:52 [WARN ] repl.MdocSession$.App: I'm warning you (.:35)
// 2021-10-25 14:08:52 [ERROR] repl.MdocSession$.App: I give up (.:36)
```


We can also re-use the program and add context to our logger:

```scala
import Logger.*
val mainWithContext: IO[Unit] = 
  for
    given Logger[IO]  <- Logger.makeIoLogger(consoleOutput)
    _                 <- program.withLogContext("trace-id", "4d334544-6462-43fa-b0b1-12846f871573")
    _                 <- Logger[IO].info("Now the context is gone")
  yield ()
```

And running with context yields:

```scala
mainWithContext.unsafeRunSync()
// 2021-10-25 14:08:52 [DEBUG] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: This is some debug (.:33)
// 2021-10-25 14:08:52 [INFO ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: HEY! (.:34)
// 2021-10-25 14:08:52 [WARN ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I'm warning you (.:35)
// 2021-10-25 14:08:52 [ERROR] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I give up (.:36)
// 2021-10-25 14:08:52 [INFO ] repl.MdocSession$.App: Now the context is gone (.:67)
```