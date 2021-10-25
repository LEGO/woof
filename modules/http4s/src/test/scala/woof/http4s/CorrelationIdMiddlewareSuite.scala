package woof.http4s

import org.http4s.HttpRoutes
import cats.effect.IO
import cats.data.Kleisli
import cats.data.OptionT
import org.http4s.Response
import cats.syntax.all.*
import woof.*
import cats.Monad
import java.time.ZoneId
import scala.concurrent.duration.*
import cats.effect.Clock
import cats.Applicative
import java.util.UUID
import org.typelevel.ci.CIString
import CorrelationIdMiddleware.UUIDGen
import org.http4s.Request
import munit.CatsEffectSuite

class CorrelationIdMiddlewareSuite extends CatsEffectSuite:

  def routes[F[_]: Monad: Logger]: HttpRoutes[F] = Kleisli(request =>
    for _ <- OptionT.liftF(Logger[F].info("Message with trace id :D"))
    yield Response[F](),
  )

  given Printer = NoColorPrinter(ZoneId.of("Europe/Copenhagen"))
  given Filter  = Filter.everything

  val testUuid = UUID.fromString("E20A27FE-5142-4E21-BA09-35BC6FB84591")
  given UUIDGen[IO] with
    def gen = testUuid.pure[IO]

  test("add trace id with middleware") {

    val myTraceHeaderName = CIString("My-Trace-Header")
    val middleWare        = CorrelationIdMiddleware.middlewareWithHeader(myTraceHeaderName)

    for
      output            <- newStringWriter
      given Logger[IO]  <- Logger.makeIoLogger(output)
      routesWithTraceId <- middleWare(routes).pure[IO]
      response          <- routesWithTraceId.run(Request[IO]()).value
      loggedString      <- output.get
    yield
      assertEquals(response.flatMap(_.headers.get(myTraceHeaderName)).map(_.head.value), testUuid.toString.some)
      assert(loggedString.contains(s"$myTraceHeaderName=$testUuid"))
    end for
  }

end CorrelationIdMiddlewareSuite
