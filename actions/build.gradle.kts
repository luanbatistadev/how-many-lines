import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/// Allow usage as `gradlew run JOB=OpenPullRequest`
val targetScript = project.gradle.startParameter.projectProperties["JOB"] ?: throw Exception("Usage `gradlew run JOB=<JOB_FILE>`")

plugins {
  kotlin("jvm") version "1.5.10"
  application
}

group = "io.lakscastro"
version = "v0.1.0"

repositories {
  mavenCentral()
}

dependencies {
  val ktorVersion = "1.6.7"

  /// Ktor library
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")

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
  mainClass.set("${targetScript}Kt")
}
