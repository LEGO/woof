package clog.local

import munit.CatsEffectSuite
import cats.effect.IO
import cats.effect.kernel.MonadCancel
import cats.effect.IOLocal
import cats.syntax.all.*

class LocalSuite extends CatsEffectSuite:

  test("local should add context") {
    import Local.given

    def getContext(using strLoc: Local[IO, String]): IO[String] = strLoc.ask

    for
      given Local[IO, String] <- Local.makeIoLocal[String]
      level1                  <- getContext.withAddedContext("CONTEXT1")
      level2                  <- getContext.withAddedContext("CONTEXT1").withAddedContext("CONTEXT2")
    yield
      assertEquals(level1, "CONTEXT1")
      assertEquals(level2, "CONTEXT2CONTEXT1")
    end for
  }

  test("Should use local context") {

  }

end LocalSuite
