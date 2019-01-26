package com.sebchlan.javacensor

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.Node
import com.github.javaparser.ast.NodeList
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.comments.LineComment
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr
import com.github.javaparser.ast.stmt.BlockStmt
import com.github.javaparser.ast.stmt.ThrowStmt
import com.github.javaparser.ast.visitor.ModifierVisitor
import com.github.javaparser.ast.visitor.Visitable
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File


class JavaCensorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("censorCopyTask", CensorCopyTask::class.java) { task ->
            task.description = "Copy source files and censor them"
        }
    }
}

open class CensorCopyTask : DefaultTask() {

    @OutputDirectory
    var destinationDir: File? = null

    @TaskAction
    fun censor() {
        val output = destinationDir ?: throw IllegalArgumentException("Please provide an output directory.")
        if (!output.isDirectory) throw IllegalArgumentException("Output is not a directory")

        project.copy { spec ->
            spec.from(inputs.files)
            spec.into(output)
        }

        output.walk()
                .filter { it.isFile }
                .forEach { censorSourceFile(it) }
    }

    private fun censorSourceFile(file: File) {
        val cu = JavaParser.parse(file)
        CensorVisitor().visit(cu, null)
        CommentVisitor().visit(cu, null)
        file.writeText(cu.toString(), Charsets.UTF_8)
    }
}

class CensorVisitor : ModifierVisitor<Void>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: Void?): Visitable? {
        return if (n.isPublic) {

            // Replace constructors
            n.constructors.forEach {
                if (it.isPublic) {
                    it.body = getBlockWithException()
                } else {
                    it.remove()
                }
            }

            // Continue visiting children
            super.visit(n, arg)

        } else {

            // Class is not public remove methods, fields and constructors
            n.methods.forEach { it.remove() }
            n.fields.forEach { it.remove() }
            n.constructors.forEach { it.remove() }

            // Continue visiting children, e.g. inner classes
            super.visit(n, arg)
        }
    }

    override fun visit(n: FieldDeclaration, arg: Void?): Visitable? {
        return if (n.isParentAnInterface() || n.isPublic) {
            // Keep field if it is public or part of an interface
            super.visit(n, arg)

        } else {
            null
        }
    }

    override fun visit(n: MethodDeclaration, arg: Void?): Visitable? {
        return when {

            // Keep method if it's part of an interface
            n.isParentAnInterface() -> {
                super.visit(n, arg)
            }

            // Keep method signature, replace body
            n.isPublic -> {
                n.setBody(getBlockWithException())
                super.visit(n, arg)
            }

            // Remove non public methods
            else -> {
                null
            }
        }
    }

    private fun Node.isParentAnInterface(): Boolean {
        return parentNode.map {
            if (it is ClassOrInterfaceDeclaration) {
                it.isInterface
            } else {
                false
            }
        }.orElseGet { false }
    }

    private fun getBlockWithException(): BlockStmt {

        val type = JavaParser.parseClassOrInterfaceType("java.lang.RuntimeException")
        val ex = ObjectCreationExpr(null, type, NodeList(StringLiteralExpr(comment)))

        val throwStmt = ThrowStmt(ex)
        throwStmt.setLineComment(javaCensorComment)

        val block = BlockStmt()
        block.addStatement(throwStmt)

        return block
    }

}

class CommentVisitor : ModifierVisitor<Void>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: Void?): Visitable? {
        if (n.members.isEmpty()) {

            n.addOrphanComment(LineComment(""))
            n.addOrphanComment(LineComment(comment))
            n.addOrphanComment(LineComment(""))
            n.addOrphanComment(LineComment(javaCensorComment))
            n.addOrphanComment(LineComment(""))
        }
        return super.visit(n, arg)
    }

}

private const val javaCensorComment = "This code was redacted by Java Censor - Learn more about it at https://github.com/schlan/java-censor"
const val comment = "Source removed"
