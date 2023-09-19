package org.legogroup.woof.http4s

import cats.data.{Kleisli, NonEmptyList, OptionT}
import cats.effect.kernel.Sync
import cats.effect.std.UUIDGen
import cats.syntax.all.*
import cats.{Applicative, FlatMap, Monad}
import org.http4s.Header.Raw
import org.http4s.{Header, HttpRoutes, Request, Response}
import org.legogroup.woof.*
import org.legogroup.woof.Logger.*
import org.typelevel.ci.CIString

import java.util.UUID

object CorrelationIdMiddleware:

  private val defaultTraceHeaderName: CIString = CIString("X-Trace-Id")

  private def getOrGenerate[F[_]: Applicative: UUIDGen](headerName: Option[CIString], request: Request[F]): F[String] =
    val key = headerName.getOrElse(defaultTraceHeaderName)
    request.headers
      .get(key)
      .map(_.head.value)
      .fold(
        summon[UUIDGen[F]].randomUUID.map(_.toString),
      )(_.pure[F])

  def middleware[F[_]: Logger: Monad: UUIDGen](headerName: Option[CIString] = None): HttpRoutes[F] => HttpRoutes[F] =
    routes =>
      Kleisli[[T] =>> OptionT[F, T], Request[F], Response[F]] { request =>
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
