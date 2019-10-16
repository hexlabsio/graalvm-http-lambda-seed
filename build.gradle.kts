import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
  kotlin("jvm") version "1.3.50"
  id("org.jlleitschuh.gradle.ktlint").version("8.1.0")
  id("com.github.johnrengelman.shadow") version "4.0.3"
  id("org.jetbrains.kotlin.plugin.serialization") version "1.3.50"
}

group = "io.hexlabs"
version = "1.0"

val http4kVersion = "3.186.0"
val heliosVersion = "0.2.0"

repositories {
  jcenter()
  mavenCentral()
  maven { url = uri("https://dl.bintray.com/hexlabsio/kloudformation") }
}

tasks.withType<ShadowJar> {
  archiveBaseName.set("dist")
  manifest {
    attributes(mapOf("Main-Class" to "io.hexlabs.AppKt"))
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

dependencies {

  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("com.amazonaws:aws-lambda-java-core:1.2.0")
  implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-runtime", version = "0.13.0")
  http4k("core", "format-jackson")
  kloudformation()
  testing()
}

fun DependencyHandler.http4k(vararg services: String) = services.forEach {
  implementation("org.http4k:http4k-$it:$http4kVersion")
}

fun DependencyHandlerScope.kloudformation() {
  testImplementation("io.kloudformation:kloudformation:1.1.19")
  testImplementation("io.hexlabs:kloudformation-serverless-module:1.1.1")
}

fun DependencyHandlerScope.testing() {
  testImplementation(group = "org.jetbrains.kotlin", name = "kotlin-test-junit5", version = "1.3.21")
  testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "1.3.21")
  testRuntime(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.0.0")
}

sourceSets {
  main {
    java {
      srcDirs("src/main/kotlin")
    }
  }
  test {
    java {
      srcDirs("src/test/kotlin", "stack")
    }
  }
}

ktlint {
  verbose.set(true)
  outputToConsole.set(true)
  coloredOutput.set(true)
  reporters.set(setOf(ReporterType.CHECKSTYLE, ReporterType.JSON))
}
