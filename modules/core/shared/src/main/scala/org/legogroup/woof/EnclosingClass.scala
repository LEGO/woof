package org.legogroup.woof

import scala.annotation.tailrec

case class EnclosingClass(fullName: String, lineLength: Int = 80):
  lazy val printableName: String = reduceNameLength(fullName)

  @tailrec
  private def reduceNameLength(name: String): String =
    List
      .unfold(name.split('.').toList) {
        case Nil                                                   => none
        case last :: Nil                                           => (last, Nil).some
        case parts @ x :: xs if parts.mkString.length > lineLength => (x.take(1), xs).some
        case x :: xs                                               => (x, xs).some
      }
      .mkString(".")
  }
