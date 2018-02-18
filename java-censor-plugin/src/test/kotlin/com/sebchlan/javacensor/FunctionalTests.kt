package com.sebchlan.javacensor

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FunctionalTests {

    @Rule @JvmField
    val testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File
    private lateinit var buildSrcDir: File

    @Before
    fun setup() {
        buildFile = testBuildFile(testProjectDir)
        buildSrcDir = testSourceDir(testProjectDir)
    }

    @Test
    fun `local class, everything gets removed, has a comment`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(testClassWithPrivateField)

        runGradle(testProjectDir, "censorSource")

        assertClassHasNoMethodsFieldsConstructors(output)
        assertClassHasComment(output)
    }

    @Test
    fun `class with private field, field gets removed`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(localTestClass)

        runGradle(testProjectDir, "censorSource")

        assertClassHasNoMethodsFieldsConstructors(output)
        assertClassHasComment(output)
    }

    @Test
    fun `class with private field, field stays untouched`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(testClassWithPublicField)

        runGradle(testProjectDir, "censorSource")

        assertClassHasNoNonPublicElements(output)
        assertClassHasPublicField(output, "String testField")
    }

    @Test
    fun `class with private method, private gets removed`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(testClassWithPrivateMethod)

        runGradle(testProjectDir,"censorSource")

        assertClassHasNoMethodsFieldsConstructors(output)
        assertClassHasComment(output)
    }

    @Test
    fun `class with public method, method throws`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(testClassWithPublicMethod)

        runGradle(testProjectDir, "censorSource")

        assertClassHasNoNonPublicElements(output)
        assertClassHasPublicMethod(output, "test")
    }

    @Test
    fun `local interface with public method, method removed`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(localTestInterface)

        runGradle(testProjectDir, "censorSource")

        assertClassHasNoNonPublicElements(output)
        assertClassHasComment(output)
    }

    @Test
    fun `public interface with public method, method throws`() {
        val (input, output) = testFile(testProjectDir, "TestClass.java")
        input.writeText(testInterface)

        runGradle(testProjectDir, "censorSource")

        assertInterfaceHasField(output,"String constant")
        assertInterfaceHasMethod(output,"test")
    }

}