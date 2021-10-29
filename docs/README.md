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

```scala mdoc:silent
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

```scala mdoc
import cats.effect.unsafe.implicits.global
main.unsafeRunSync()
```


We can also re-use the program and add context to our logger:

```scala mdoc:silent
import Logger.*
val mainWithContext: IO[Unit] = 
  for
    given Logger[IO]  <- Logger.makeIoLogger(consoleOutput)
    _                 <- program.withLogContext("trace-id", "4d334544-6462-43fa-b0b1-12846f871573")
    _                 <- Logger[IO].info("Now the context is gone")
  yield ()
```

And running with context yields:

```scala mdoc
mainWithContext.unsafeRunSync()
```

# Can I use SLF4J?

Yes, you can. I don't think you should, but you can! Note, however, that not everything can be implemented perfectly against the
slf4j-api, e.g. the filtering functionality in `woof` is much more flexible and thus does not map directly to `isDebugEnabled` since 
woof-filters are not limited to log level.

```scala mdoc
import org.slf4j.LoggerFactory
def programWithSlf4j: IO[Unit] = 
  for
    slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
    _ <- IO.delay(slf4jLogger.info("Hello from SLF4j!"))
    _ <- IO.delay(slf4jLogger.warn("This is not the pure woof."))
  yield ()
```

To run this program with woof

1. add `woof-slf4j` as a dependency to our program
1. instantiate a `woof.Logger[F[_]]` as per usual
1. register the _woof logger_ to the static log binder to allow the slf4j `LoggerFactory` to find it.

> Note that any logs that happen before registering are lost!

```scala mdoc:silent
import woof.slf4j.*
val mainSlf4j: IO[Unit] = 
  for
    woofLogger  <- Logger.makeIoLogger(consoleOutput)
    _           <- woofLogger.registerSlf4j
    _           <- programWithSlf4j
  yield ()
```

and running it:

```scala mdoc
mainSlf4j.unsafeRunSync()
```
## Limitations of SLF4J bindings

Currently, markers do nothing. You can get the same behaviour easily with context when using the direct __Woof__ api with filters and printers.

# Can I use __http4s__?

> TODO: Add example