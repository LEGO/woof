package org.legogroup.woof

import cats.kernel.Monoid
import cats.syntax.order.*

import scala.util.matching.Regex

case class LogLine(level: LogLevel, info: LogInfo, message: String, context: List[(String, String)])

enum Filter:
  private case AtLeastLevel(level: LogLevel)
  private case ExactLevel(level: LogLevel)
  private case ClassRegex(regex: Regex)
  private case MessageFilter(filter: String => Boolean)
  private case LineNumberFilter(filter: Int => Boolean)
  private case CompositeAnd(a: Filter, b: Filter)
  private case CompositeOr(a: Filter, b: Filter)
  private case Nothing
  private case Everything
end Filter

object Filter:

  val atLeastLevel: LogLevel => Filter               = level => AtLeastLevel(level)
  val exactLevel: LogLevel => Filter                 = level => ExactLevel(level)
  val regexFilter: Regex => Filter                   = regex => ClassRegex(regex)
  val nothing: Filter                                = Nothing
  val everything: Filter                             = Everything
  def lineNumberFilter(test: Int => Boolean): Filter = LineNumberFilter(test)
  def messageFilter(test: String => Boolean): Filter = MessageFilter(test)

  given Monoid[Filter] with
    def empty: Filter                         = nothing
    def combine(f: Filter, g: Filter): Filter = f or g

  extension (f: Filter)
    infix def and(g: Filter): Filter = Filter.CompositeAnd(f, g)
    infix def or(g: Filter): Filter  = Filter.CompositeOr(f, g)
    def interpret: LogLine => Boolean = line =>
      f match
        case Filter.AtLeastLevel(level)      => line.level >= level
        case Filter.ExactLevel(level)        => line.level == level
        case Filter.ClassRegex(regex)        => regex.matches(line.info.enclosingClass.printableName)
        case Filter.CompositeAnd(a, b)       => a.interpret(line) && b.interpret(line)
        case Filter.CompositeOr(a, b)        => a.interpret(line) || b.interpret(line)
        case Filter.LineNumberFilter(filter) => filter(line.info.lineNumber)
        case Filter.MessageFilter(filter)    => filter(line.message)
        case Filter.Nothing                  => false
        case Filter.Everything               => true
  end extension

end Filter
