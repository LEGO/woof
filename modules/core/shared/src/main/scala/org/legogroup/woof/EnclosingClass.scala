package org.legogroup.woof

case class EnclosingClass(fullName: String, lineLength: Int = 80):
  lazy val printableName: String = reduceNameLength(fullName)

  private def reduceNameLength(name: String): String =
    List
      .unfold(name.split('.').toList) {
        case Nil                                                        => None
        case last :: Nil                                                => Some(last, Nil)
        case parts @ x :: xs if parts.mkString(".").length > lineLength => Some(x.take(1), xs)
        case x :: xs                                                    => Some(x, xs)
      }
      .mkString(".")
end EnclosingClass
