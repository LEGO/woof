package org.legogroup.woof

import java.io.File
import java.nio.file.Paths
import scala.quoted.*

object Logging:

  case class LogInfo(enclosingClass: String, file: File, lineNumber: Int):
    def prefix: String  = enclosingClass
    def postfix: String = s"(${file.getName}:${lineNumber + 1})"

  object Macro:

    given ToExpr[File] with
      def apply(f: File)(using Quotes): Expr[File] =
        val path = Expr(f.getAbsolutePath)
        '{ new java.io.File($path) }

    def enclosingClass(using q: Quotes)(symb: quotes.reflect.Symbol): quotes.reflect.Symbol =
      if symb.isClassDef then symb else enclosingClass(symb.owner)

    def logInfo(s: Expr[Any])(using Quotes): Expr[LogInfo] =
      import quotes.reflect.*

      val cls      = enclosingClass(Symbol.spliceOwner)
      val name     = cls.fullName
      val nameExpr = Expr(name)

      val position   = Position.ofMacroExpansion
      val filePath   = if position.sourceFile.jpath != null then position.sourceFile.jpath else Paths.get(".")
      val lineNumber = Expr(position.startLine)
      val file       = Expr(File(filePath.toString))

      '{ LogInfo($nameExpr, $file, $lineNumber) }
    end logInfo

  end Macro

  inline def info(x: Any): LogInfo = ${ Macro.logInfo('x) }

end Logging
