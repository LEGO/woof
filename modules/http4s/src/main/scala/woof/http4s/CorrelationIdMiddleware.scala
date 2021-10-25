package woof.http4s
import cats.Applicative
import cats.FlatMap
import cats.Monad
import cats.data.Kleisli
import cats.data.NonEmptyList
import cats.data.OptionT
import cats.syntax.all.*
import org.http4s.Header.Raw
import org.http4s.HttpRoutes
import org.http4s.Request
import org.http4s.Response
import org.typelevel.ci.CIString
import woof.*

import java.util.UUID

import Logger.*
import org.http4s.Header
import cats.effect.kernel.Sync

object CorrelationIdMiddleware:

  private val defaultTraceHeaderName: CIString = CIString("X-Trace-Id")

  trait UUIDGen[F[_]]:
    def gen: F[UUID]
  given [F[_]: Sync]: UUIDGen[F] = new UUIDGen[F]:
    def gen = Sync[F].delay(UUID.randomUUID)

  private def getOrGenerate[F[_]: Applicative: UUIDGen](headerName: Option[CIString], request: Request[F]): F[String] =
    val key = headerName.getOrElse(defaultTraceHeaderName)
    request.headers
      .get(key)
      .map(_.head.value)
      .fold(
        summon[UUIDGen[F]].gen.map(_.toString),
      )(_.pure[F])

  def middleware[F[_]: Logger: Monad: UUIDGen](headerName: Option[CIString] = None): HttpRoutes[F] => HttpRoutes[F] =
    routes =>
      Kleisli[([T] =>> OptionT[F, T]), Request[F], Response[F]] { request =>
        val key = headerName.getOrElse(defaultTraceHeaderName)
        for
          traceId <- OptionT.liftF(getOrGenerate(headerName, request))
          result  <- OptionT(routes.run(request).value.withLogContext(key.toString, traceId))
        yield result.putHeaders(Header.Raw(key, traceId))
      }

  def middlewareWithHeader(
      headerName: CIString,
  ): [F[_]] => Logger[F] ?=> Monad[F] ?=> UUIDGen[F] ?=> HttpRoutes[F] => HttpRoutes[F] =
    [F[_]] => (_: Logger[F]) ?=> (_: Monad[F]) ?=> (_: UUIDGen[F]) ?=> middleware[F](headerName.some)

end CorrelationIdMiddleware
