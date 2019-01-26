package com.sebchlan.javacensor

import org.gradle.testkit.runner.GradleRunner
import org.junit.rules.TemporaryFolder
import java.io.File
import java.net.URLDecoder


fun testFile(testProject: TemporaryFolder, name: String): Pair<File, File> {
    val file = testProject.newFile("src/$name")
    val resultFile = File(testProject.root, "censored_source/$name")
    return Pair(file, resultFile)
}

fun runGradle(folder: TemporaryFolder, argument: String) = GradleRunner.create().apply {
    withPluginClasspath(pluginPath())
    withProjectDir(folder.root)
    withArguments(argument)
}.build()!!


fun testSourceDir(folder: TemporaryFolder): File = folder.newFolder("src")

fun testBuildFile(folder: TemporaryFolder): File {

    val buildFile = """
buildscript {
    dependencies {
        classpath files(${pluginPath().joinToString(separator = ",") { "'${it.absolutePath }'" }})
    }
}

apply plugin: 'com.sebchlan.javacensor'

task censorSource(type: com.sebchlan.javacensor.CensorCopyTask) {
    inputs.files(files("${folder.root}/src").toSet())
    destinationDir = file("${folder.root}/censored_source")
}
"""
    val file = folder.newFile("build.gradle")
    file.writeText(buildFile)
    return file
}

private fun pluginPath(): List<File> {
    return File(URLDecoder.decode(ClassLoader.getSystemResource("plugin-classpath.txt").file, "UTF-8"))
            .readLines()
            .map { File(it) }
}
