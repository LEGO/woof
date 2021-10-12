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
import cats.effect.kernel.Sync
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode
import cats.effect.std.Console
import clog.*

object MainCats extends IOApp:

  val consoleOutput: Output[IO] = new Output[IO]:
    def output(str: String)      = IO.delay(println(str))
    def outputError(str: String) = output(str) // MDOC ignores stderr

  given Printer = NoColorPrinter()
  val logger = new Logger[IO](consoleOutput)

  override def run(args: List[String]): IO[ExitCode] =
    for
      _ <- logger.debug("This is some debug")
      _ <- logger.info("HEY!")
      _ <- logger.warn("I'm warning you")
      _ <- logger.error("I give up")
    yield ExitCode.Success

end MainCats
```

and running it yields:

```scala
import cats.effect.unsafe.implicits.global
MainCats.run(Nil).unsafeRunSync()
// 12:31:51 [DEBUG] repl.MdocSession$.App.MainCats$: This is some debug (.:36)
// 12:31:51 [INFO ] repl.MdocSession$.App.MainCats$: HEY! (.:37)
// 12:31:51 [WARN ] repl.MdocSession$.App.MainCats$: I'm warning you (.:38)
// 12:31:51 [ERROR] repl.MdocSession$.App.MainCats$: I give up (.:39)
// res0: ExitCode = ExitCode(0)
```