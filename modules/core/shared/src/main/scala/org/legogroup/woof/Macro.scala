package org.legogroup.woof

import java.io.File
import java.nio.file.Paths
import scala.annotation.tailrec
import scala.quoted.*

object Macro:

  private given ToExpr[File] with
    def apply(f: File)(using Quotes): Expr[File] =
      val path = Expr(f.getAbsolutePath)
      '{ new java.io.File($path) }

  private given ToExpr[EnclosingClass] with
    def apply(name: EnclosingClass)(using Quotes): Expr[EnclosingClass] =
      val exp = Expr(name.fullName)
      '{ EnclosingClass($exp) }

  @tailrec
  private def enclosingClass(using q: Quotes)(symb: quotes.reflect.Symbol): quotes.reflect.Symbol =
    if symb.isClassDef then symb else enclosingClass(symb.owner)

  private def logInfo(using Quotes): Expr[LogInfo] =
    import quotes.reflect.*

    val cls      = enclosingClass(Symbol.spliceOwner)
    val name     = cls.fullName
    val nameExpr = Expr(EnclosingClass(name))

    val position   = Position.ofMacroExpansion
    val filePath   = if position.sourceFile.path != null then position.sourceFile.path else Paths.get(".").getFileName
    val lineNumber = Expr(position.startLine)
    val file       = Expr(filePath.toString.split("/").takeRight(1).mkString)

    '{ LogInfo($nameExpr, $file, $lineNumber) }
  end logInfo

  inline given LogInfo = ${ logInfo }

end Macro

export Macro.given_LogInfo
