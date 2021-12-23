package org.legogroup.woof.http4s

import cats.data.{Kleisli, OptionT}
import cats.effect.{Clock, IO}
import cats.syntax.all.*
import cats.{Applicative, Monad}
import munit.CatsEffectSuite
import org.http4s.{HttpRoutes, Request, Response}
import org.legogroup.woof.{given, *}
import org.legogroup.woof.http4s.CorrelationIdMiddleware.UUIDGen
import org.typelevel.ci.CIString

import java.time.ZoneId
import java.util.UUID
import scala.concurrent.duration.*

class CorrelationIdMiddlewareSuite extends CatsEffectSuite:

  def routes[F[_]: Monad: Logger]: HttpRoutes[F] = Kleisli(request =>
    for _ <- OptionT.liftF(Logger[F].info("Message with trace id :D"))
    yield Response[F](),
  )

  given Printer = NoColorPrinter()
  given Filter  = Filter.everything

  val testUuid = UUID.fromString("E20A27FE-5142-4E21-BA09-35BC6FB84591")
  given UUIDGen[IO] with
    def gen = testUuid.pure[IO]

  test("add trace id with middleware") {

    val myTraceHeaderName = CIString("My-Trace-Header")
    val middleWare        = CorrelationIdMiddleware.middlewareWithHeader(myTraceHeaderName)

    for
      output            <- newStringWriter
      given Logger[IO]  <- DefaultLogger.makeIo(output)
      routesWithTraceId <- middleWare(routes).pure[IO]
      response          <- routesWithTraceId.run(Request[IO]()).value
      loggedString      <- output.get
    yield
      assertEquals(response.flatMap(_.headers.get(myTraceHeaderName)).map(_.head.value), testUuid.toString.some)
      assert(loggedString.contains(s"$myTraceHeaderName=$testUuid"))
    end for
  }

end CorrelationIdMiddlewareSuite
