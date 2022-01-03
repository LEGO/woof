package org.legogroup.woof

import cats.kernel.Monoid
import cats.syntax.order.*

import scala.util.matching.Regex

case class LogLine(level: LogLevel, info: LogInfo, message: String, context: List[(String, String)])

type Filter = LogLine => Boolean

object Filter:

  val atLeastLevel: LogLevel => Filter = level => line => line.level >= level
  val exactLevel: LogLevel => Filter   = level => line => line.level == level
  val regexFilter: Regex => Filter     = regex => line => regex.matches(line.info.enclosingClass.fullName)
  val nothing: Filter                  = _ => false
  val everything: Filter               = _ => true

  given Monoid[Filter] with
    def empty: Filter                         = nothing
    def combine(f: Filter, g: Filter): Filter = f or g

end Filter

extension (f: Filter)
  infix def and(g: Filter): Filter = line => f(line) && g(line)
  infix def or(g: Filter): Filter  = line => f(line) || g(line)
