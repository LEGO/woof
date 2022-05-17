package org.legogroup.woof.incubator.extensions

import munit.CatsEffectSuite
import org.legogroup.woof.{*, given}
import cats.effect.IO
import cats.syntax.all.*
import cats.instances.string
import cats.effect.kernel.Clock
import scala.concurrent.duration.*
class ExtensionsSuite extends CatsEffectSuite:

  given Filter  = Filter.everything
  given Printer = NoColorPrinter(testFormatTime)

  test("log methods directly on F") {

    given Clock[IO] = leetClock

    val ioValue = IO.pure("foo")

    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)
      _                <- ioValue.infoShow
      logs             <- stringWriter.get
    yield assertEquals(
      logs,
      "1987-05-31 13:37:00 [INFO ] org.legogroup.woof.incubator.extensions.ExtensionsSuite: foo (ExtensionsSuite.scala:24)\n"
    )

  }

  test("traverselog") {
    val items = List("foo", "bar", "baz")

    val expected = List(
      "33.33%",
      "66.67%",
      "100.00%"
    ).zip(items)
      .map((p, n) =>
        s"1987-05-31 13:37:00 [DEBUG] org.legogroup.woof.incubator.extensions.ExtensionsSuite: Item $n, $p (ExtensionsSuite.scala:50)"
      )
      .mkString("", "\n", "\n")

    given Clock[IO] = leetClock
    for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)
      _                <- items.traverseLog(_.pure[IO], (item, percentage) => f"Item $item, $percentage%.2f%%")
      logs             <- stringWriter.get
    yield assertEquals(logs, expected),

  }

  test("parTraverselog") {
    val items = List("foo", "bar", "baz")

    val expected = List(
      "33.33%",
      "66.67%",
      "100.00%"
    )

    val program = for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)
      result           <- items.parTraverseLog(2)(IO.pure, (item, percentage) => f"Item $item, $percentage%.2f%%")
      logs             <- stringWriter.get
    yield expected.foreach(p => assert(logs.contains(p), s"should contain $p"))

    executeWithStartTime(program)
  }

  test("parTraverseLogProgress") {
    val items = List(5, 5, 5)
    val expected = List(
      ("0.00%", "04"),
      ("33.33%", "09"),
      ("66.67%", "14")
    ).map((p, s) =>
      s"1987-05-31 13:37:$s [DEBUG] org.legogroup.woof.incubator.extensions.ExtensionsSuite: Progress is now at: $p (ExtensionsSuite.scala:89)"
    ).mkString("", "\n", "\n")

    given Sleep[IO] = IO.sleep
    val program = for
      stringWriter     <- newStringWriter
      given Logger[IO] <- DefaultLogger.makeIo(stringWriter)
      _ <- items.parTraverseLogProgress(parallelism = 1, every = 4.9.seconds)(
        i => IO.sleep(i.seconds),
        d => f"Progress is now at: $d%.2f%%"
      )
      logs <- stringWriter.get
    yield assertEquals(logs, expected)

    executeWithStartTime(program)
  }

end ExtensionsSuite
