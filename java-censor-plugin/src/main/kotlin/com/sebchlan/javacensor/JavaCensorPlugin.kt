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
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.concurrent.atomic.AtomicInteger


class JavaCensorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val task = project.tasks.create("censorCopyTask", CensorCopyTask::class.java)
        task.description = "Copy source files and censor them"
    }
}

open class CensorCopyTask : DefaultTask() {

    @Suppress("MemberVisibilityCanBePrivate")
    var from: Set<File> = emptySet()

    @Suppress("MemberVisibilityCanBePrivate")
    var into: File? = null

    @TaskAction
    fun censor() {
        val output = into ?: throw IllegalArgumentException("Please provide an output directory.")

        project.copy { spec ->
            spec.from(from)
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
        val ex = ObjectCreationExpr(null, type, NodeList())
        ex.addArgument(StringLiteralExpr(exceptionText.nextElement()))

        val throwStmt = ThrowStmt(ex)

        val block = BlockStmt()
        block.addStatement(throwStmt)

        return block
    }

}

class CommentVisitor : ModifierVisitor<Void>() {

    override fun visit(n: ClassOrInterfaceDeclaration, arg: Void?): Visitable? {
        if (n.members.isEmpty()) {
            n.addOrphanComment(LineComment(commentList.nextElement()))
        }
        return super.visit(n, arg)
    }

}

private val index = AtomicInteger()
private fun List<String>.nextElement(): String = get(index.getAndIncrement() % size)
val commentList = listOf("¯\\_(ツ)_/¯", "Oh Hai Mark!", "Let’s go eat, huh?", "Hi, doggie.", "Ha ha ha ha ha. What a story Mark!", "Denny, two is great, but three is a crowd.", " I cannot tell you, its confidential.", "Chirp chirp chirp chirp!", "You are tearing me apart Lisa!")
val exceptionText = "It's in theaters now! Coming this summer: Two brothers. In a van. And then a meteor hit. And they ran as fast as they could, from giant cat monsters. And then a giant tornado came and that's when things got knocked into 12th gear. A Mexican armada shows up. With weapons made from Two--tomatoes. And you better bet your bottom dollar that these two brothers know how to handle business. In: Alien Invasion Tomato Monster Mexican Armada Brothers, Who Are Just Regular Brothers, Running In a van from an Asteroid and All Sorts of Things THE MOVIE! Hold on, there's more! Old women are coming, and they're also in the movie, and they're gonna come, and cross attack these two brothers. But let's get back to the brothers, because they're-- they have a strong bond. You don't want to know about it here, but I'll tell you one thing: The moon it comes crashing into Earth. And what do you do then? It's two brothers and--and th-they're It's called Two brothers. Two brothers! It's just called Two Brothers.".split(",", ".", "/", "\\", "?", "!", ";", "\"", ":", "-").map { it.trim() }
