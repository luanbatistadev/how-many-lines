import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/// Allow usage as `gradlew run -P JOB=OpenPullRequest`
val targetScript = project.gradle.startParameter.projectProperties["JOB"]

plugins {
  kotlin("jvm") version "1.5.10"
  application
}

group = "io.lakscastro.howmanylines"
version = "v0.1.0"

repositories {
  mavenCentral()
}

dependencies {
  val ktorVersion = "1.6.7"

  /// OkHttp library to call GitHub API
  implementation("com.squareup.okhttp3:okhttp:4.9.3")

  /// Turtle library, to run shell commands
  implementation("com.lordcodes.turtle:turtle:0.6.0")

  /// Json Parser
  implementation("org.json:json:20211205")

  /// Test dependencies
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnit()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}

application {
  mainClass.set("$group.${targetScript}Kt")
}
