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
// 2021-11-01 08:51:34 [DEBUG] repl.MdocSession$.App: This is some debug (.:33)
// 2021-11-01 08:51:34 [INFO ] repl.MdocSession$.App: HEY! (.:34)
// 2021-11-01 08:51:34 [WARN ] repl.MdocSession$.App: I'm warning you (.:35)
// 2021-11-01 08:51:34 [ERROR] repl.MdocSession$.App: I give up (.:36)
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
// 2021-11-01 08:51:34 [DEBUG] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: This is some debug (.:33)
// 2021-11-01 08:51:34 [INFO ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: HEY! (.:34)
// 2021-11-01 08:51:34 [WARN ] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I'm warning you (.:35)
// 2021-11-01 08:51:34 [ERROR] trace-id=4d334544-6462-43fa-b0b1-12846f871573 repl.MdocSession$.App: I give up (.:36)
// 2021-11-01 08:51:34 [INFO ] repl.MdocSession$.App: Now the context is gone (.:67)
```

# Can I use SLF4J?

Yes, you can. I don't think you should, but you can! Note, however, that not everything can be implemented perfectly against the
slf4j-api, e.g. the filtering functionality in `woof` is much more flexible and thus does not map directly to `isDebugEnabled` since 
woof-filters are not limited to log level.

```scala
import org.slf4j.LoggerFactory
def programWithSlf4j: IO[Unit] = 
  for
    slf4jLogger <- IO.delay(LoggerFactory.getLogger(this.getClass))
    _           <- IO.delay(slf4jLogger.info("Hello from SLF4j!"))
    _           <- IO.delay(slf4jLogger.warn("This is not the pure woof."))
  yield ()
```

To run this program with woof

1. add `woof-slf4j` as a dependency to our program
1. instantiate a `woof.Logger[F[_]]` as per usual
1. register the _woof logger_ to the static log binder to allow the slf4j `LoggerFactory` to find it.

> Note that any logs that happen before registering are lost!

```scala
import woof.slf4j.*
val mainSlf4j: IO[Unit] = 
  for
    woofLogger  <- Logger.makeIoLogger(consoleOutput)
    _           <- woofLogger.registerSlf4j
    _           <- programWithSlf4j
  yield ()
```

and running it:

```scala
mainSlf4j.unsafeRunSync()
// 2021-11-01 08:51:34 [INFO ] repl.MdocSession$App: Hello from SLF4j! (MdocSession$App.scala:87)
// 2021-11-01 08:51:34 [WARN ] repl.MdocSession$App: This is not the pure woof. (MdocSession$App.scala:88)
```
## Limitations of SLF4J bindings

Currently, markers do nothing. You can get the same behaviour easily with context when using the direct __Woof__ api with filters and printers.

# Can I use __http4s__?

```scala
import org.http4s.{HttpRoutes, Response}
import cats.data.{Kleisli, OptionT}
import cats.syntax.functor.given

def routes(using Logger[IO]): HttpRoutes[IO] =
  Kleisli(request =>
    OptionT
      .liftF(Logger[IO].info("I got a request with trace id! :D"))
      .as(Response[IO]()),
  )
```

We create a tracing middleware from the above routes and call the resulting
route with an empty request.

```scala
import org.http4s.Request
import woof.http4s.CorrelationIdMiddleware
import cats.syntax.option.given

val mainHttp4s: IO[Unit] = 
  for
    given Logger[IO]  <- Logger.makeIoLogger(consoleOutput)
    maybeResponse     <- CorrelationIdMiddleware.middleware[IO]()(routes).run(Request[IO]()).value
    responseHeaders   =  maybeResponse.map(_.headers).orEmpty
    _                 <- Logger[IO].info(s"Got response headers: $responseHeaders")
  yield ()
```

Finally, running it, we see that the correlation ID is added to the log message inside the routes (transparently), and that 
the correlation ID is also returned in the header of the response.

> NOTE: The correlation ID is _not_ present outside the routes, i.e. we have scoped it only to the service part of our code.

```scala
mainHttp4s.unsafeRunSync()
// 2021-11-01 08:51:35 [INFO ] X-Trace-Id=c66c2587-9d4d-44d7-9858-6f9be6e330ef repl.MdocSession$.App: I got a request with trace id! :D (.:127)
// 2021-11-01 08:51:35 [INFO ] repl.MdocSession$.App: Got response headers: Headers(X-Trace-Id: c66c2587-9d4d-44d7-9858-6f9be6e330ef) (.:148)
```