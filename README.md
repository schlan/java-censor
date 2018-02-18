
<p align="center">
<a href="https://github.com/schlan/java-censor"><img src="https://i.imgur.com/P4jbfWm.png" title="source: imgur.com" /></a>
<br/>
<a href="https://travis-ci.org/schlan/java-censor"><img src="https://travis-ci.org/schlan/java-censor.svg?branch=master" alt="Build Status" /></a>
<img src="https://img.shields.io/maven-central/v/com.sebchlan.java/java-censor.svg" alt="Java Censor Version" />
<a href="https://oss.sonatype.org/content/repositories/snapshots/com/sebchlan/javacensor/java-censor/"> <img src="https://img.shields.io/nexus/s/https/oss.sonatype.org/com.sebchlan.javacensor/java-censor.svg" /></a>
<a href="https://github.com/schlan/java-censor/blob/master/LICENSE.md"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License" /></a>
</p>

Java Censor is a Gradle plugin that enables developers to publish sources of closed source projects to a Maven repository. It does this by removing implemented code only leaving the signatures of public interfaces, classes, methods and fields behind. 
<img src="https://i.imgur.com/XO8EQ1a.png" />


## Purpose

Java Censor is built for closed source java libraries, in particular, closed source Android libraries. When working on closed source libraries, it is usually not possible to ship a source artefact when deploying a release. 
That brings one big downside* for the integrators of your library. Even if you ship a Javadoc artefact, IDEs like Android Studio or IntelliJ don't use that artefact for showing documentation. Instead, they rely solely on the source artefact for showing inline documentation.  

That is were Java Censor can help. Java Censor makes it possible to release a source artefact of a closed source library without exposing secret code but still allows IDEs to show inline documentation. It does that when running before the release, by removing any implemented code, only keeping public signatures (interfaces, classes, methods, constructors, fields) and Javadoc. 

## Usage

For using Java Censor in a Gradle project, it needs to be added to the project first. That is done by adding it as a classpath dependency to the `buildscript` block and applying the plugin to a module. 
Applying the plugin to a module introduces a new task type called `CensorCopyTask`.  The `CensorCopyTask` behaves like the built-in `Copy` task and has two mandatory configuration options.

<dl>
  <dt>from = [set of files]</dt>
  <dd>Takes a set of files as input.</dd>

  <dt>into = [output directory]</dt>
  <dd>The output directory. (File)</dd>
</dl>

Example:

```gradle
// Add classpath dependency
buildscript {
  dependencies {
    classpath group: 'com.sebchlan.javacensor', name: 'java-censor', version: '1.0.0'
  }
}

// Apply the plugin
apply plugin: 'com.sebchlan.javacensor'

// Add a new task
task censorSource(type: com.sebchlan.javacensor.CensorCopyTask) {
    from = sourceSets.main.java.srcDirs
    into = file("$buildDir/censored_source")
}
```

### Android

Java Censor is also applicable to Android projects. The needed steps are very similar to the ones above. 
This is how a configuration can look like:

```gradle

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath group: 'com.sebchlan.javacensor', name: 'java-censor', version: '1.0.0'
    }
}

apply plugin: 'com.sebchlan.javacensor'

afterEvaluate { project ->

    uploadArchives {
        repositories {
            mavenDeployer {
                // ....
            }
        }

        task censorSource(type: com.sebchlan.javacensor.CensorCopyTask) {
            from = android.sourceSets.main.java.srcDirs
            into = file("$buildDir/generated-src")
        }

        task androidSourcesJar(type: Jar) {
            classifier = 'sources'
            from "$buildDir/generated-src/"
            dependsOn censorSource
        }

        artifacts {
            archives androidSourcesJar
        }
        
    }
}
```

## Contribution 

Feel free to submit a PR or file an issue. Please make sure, when filing a bug to provided sufficient reproduction steps, or even a failing test. 
