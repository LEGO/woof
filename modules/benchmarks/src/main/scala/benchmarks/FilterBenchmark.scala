package benchmarks

import cats.effect.IO
import cats.syntax.all.*
import org.legogroup.woof.{*, given}
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole
import cats.effect.unsafe.implicits.global

import scala.annotation.tailrec

class FilterBenchmark:

  def test(blackhole: Blackhole)(using Filter): IO[Unit] =

    val blackholeOutput: Output[IO] = new:
      override def output(str: String): IO[Unit]      = IO.delay(blackhole.consume(str))
      override def outputError(str: String): IO[Unit] = IO.delay(blackhole.consume(str))

    given Printer = ColorPrinter()
    for
      logger <- DefaultLogger.makeIo(blackholeOutput)
      _      <-
        def loop(remaining: Int): IO[Unit] =
          val message = s"message$remaining"
          if remaining <= 0 then IO.unit
          else
            for
              _ <- logger.info(message)
              _ <- logger.warn(message)
              _ <- logger.error(message)
              _ <- loop(remaining - 1)
            yield ()
        end loop
        loop(1000)
    yield ()
    end for
  end test

  @Benchmark
  def testEverything(blackhole: Blackhole): Unit = test(blackhole)(using Filter.everything).unsafeRunSync()

  @Benchmark
  def testNothing(blackhole: Blackhole): Unit = test(blackhole)(using Filter.nothing).unsafeRunSync()

  @Benchmark
  def testInfo(blackhole: Blackhole): Unit = test(blackhole)(using Filter.exactLevel(LogLevel.Info)).unsafeRunSync()

end FilterBenchmark
