package org.legogroup.woof

case class EnclosingClass(fullName: String, lineLength: Int = 80):
  lazy val printableName: String = reduceNameLength(fullName)

  import cats.syntax.option.*

  private def chop(xs: List[String]): List[String] = xs.map(_.take(1))

  private def reduceNameLength(name: String): String =
    val parts = name.split('.').toList

    val packagePrefix   = parts.dropRight(1)
    val suffixClassName = parts.lastOption.orEmpty

    val possibilities =
      packagePrefix.indices
        .map(packagePrefix.splitAt)
        .map((prefix, suffix) => (chop(prefix) ++ suffix :+ suffixClassName).mkString("."))
        .filter(_.length <= lineLength)

    val maxChoppedFallback = (chop(packagePrefix) :+ suffixClassName).mkString(".")

    possibilities
      .maxByOption(_.length)
      .getOrElse(maxChoppedFallback)
  end reduceNameLength
end EnclosingClass
