package woof

import cats.effect.{IO, Ref}
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import scala.concurrent.duration.*
import cats.effect.kernel.Clock
import cats.Applicative
import cats.syntax.all.*
class StringWriter(val ref: Ref[IO, String]) extends Output[IO]:
  def output(str: String): IO[Unit]      = ref.update(log => s"$log$str\n")
  def outputError(str: String): IO[Unit] = output(str)
  def get: IO[String]                    = ref.get

val newStringWriter: IO[StringWriter] =
  for ref <- Ref[IO].of("")
  yield StringWriter(ref)

val testFormatTime = (i: Instant) =>
  DateTimeFormatter
    .ofPattern("HH:mm:ss")
    .withZone(ZoneId.of("Europe/Copenhagen"))
    .format(i)

val startTime = 549459420.seconds
val leetClock: Clock[IO] = new Clock[IO]:
  def applicative = Applicative[IO]
  def monotonic   = startTime.pure
  def realTime    = startTime.pure
