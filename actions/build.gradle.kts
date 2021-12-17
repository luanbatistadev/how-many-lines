import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
  mainClass.set("MainKt")
}


