package com.sebchlan.javacensor

import com.github.javaparser.JavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Assert.fail
import java.io.File

fun assertClassHasNoMethodsFieldsConstructors(sourceFile: File) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    assertWithMessage("Ensure class has no methods")
            .that(clazz.methods).isEmpty()

    assertWithMessage("Ensure class has no fields")
            .that(clazz.fields).isEmpty()

    assertWithMessage("Ensure class has no constructors")
            .that(clazz.constructors).isEmpty()
}

fun assertClassHasComment(sourceFile: File) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    val hasComment = clazz.orphanComments.map { commentList.contains(it.content.trim()) }.contains(true)
    assertWithMessage("Ensure class has java censor comment")
            .that(hasComment).isTrue()
}

fun assertClassHasNoNonPublicElements(sourceFile: File) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    assertWithMessage("Ensure class has no private/local/protected methods")
            .that(clazz.methods.filter { !it.isPublic }).isEmpty()

    assertWithMessage("Ensure class has no private/local/protected fields")
            .that(clazz.fields.filter { !it.isPublic }).isEmpty()

    assertWithMessage("Ensure class has no private/local/protected constructors")
            .that(clazz.constructors.filter { !it.isPublic }).isEmpty()

}

fun assertClassHasPublicField(sourceFile: File, fieldName: String) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    val hasField = clazz.fields.filter { it.isPublic && it.toString().contains(fieldName) }

    assertWithMessage("Ensure class has a public field with name $fieldName")
            .that(hasField).hasSize(1)
}

fun assertClassHasPublicMethod(sourceFile: File, fieldName: String) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    val method = clazz.methods.filter { it.isPublic && it.nameAsString == fieldName }

    assertWithMessage("Ensure class has a public method with name '$fieldName'")
            .that(method).hasSize(1)

    val body = method.first().body.get()

    assertWithMessage("Ensure class has a public method with name '$fieldName' that has one throw statement")
            .that(body.statements.size == 1 && body.statements[0].isThrowStmt).isTrue()
}

fun assertInterfaceHasField(sourceFile: File, fieldName: String) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    val hasField = clazz.fields.filter { it.toString().contains(fieldName) }

    assertWithMessage("Ensure class has a field with name $fieldName")
            .that(hasField).hasSize(1)
}

fun assertInterfaceHasMethod(sourceFile: File, methodName: String) {
    val cu = readSourceFile(sourceFile)
    val clazz = getFirstClass(cu)

    val method = clazz.methods.filter { it.nameAsString == methodName }

    assertWithMessage("Ensure class has a method with name '$methodName'")
            .that(method).hasSize(1)
}


private fun getFirstClass(cu: CompilationUnit): ClassOrInterfaceDeclaration  {
    val clazz = cu.types.filterIsInstance<ClassOrInterfaceDeclaration>()
    assertThat(clazz.size).isEqualTo(1)
    return clazz.first()
}

private fun readSourceFile(sourceFile: File): CompilationUnit {

    val cu = try {
        JavaParser.parse(sourceFile)
    } catch (e: Exception) {
        fail("Unable to parse file. Error ${e.message}")
        null
    }

    if (cu == null) {
        fail("Something is wrong. JavaParser returned null")
    }

    return cu!!
}