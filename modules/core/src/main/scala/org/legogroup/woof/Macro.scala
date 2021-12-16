package org.legogroup.woof

import java.io.File
import java.nio.file.Paths
import scala.annotation.tailrec
import scala.quoted.*

object Macro:

  @tailrec
  private def enclosingClass(using q: Quotes)(symb: quotes.reflect.Symbol): quotes.reflect.Symbol =
    if symb.isClassDef then symb else enclosingClass(symb.owner)

  private def logInfo(s: Expr[Any])(using Quotes): Expr[LogInfo] =
    import quotes.reflect.*

    val cls      = enclosingClass(Symbol.spliceOwner)
    val name     = cls.fullName
    val nameExpr = Expr(name)

    val position   = Position.ofMacroExpansion
    val filePath   = if position.sourceFile.jpath != null then position.sourceFile.jpath else Paths.get(".")
    val lineNumber = Expr(position.startLine)
    val file       = Expr(File(filePath.toString).getName)

    '{ LogInfo($nameExpr, $file, $lineNumber) }
  end logInfo

  inline def expressionInfo(x: Any): LogInfo = ${ logInfo('x) }

end Macro
