package io.lakscastro.howmanylines.shell

import io.lakscastro.howmanylines.utils.*

/// Execute the CLI module that can be found in `~/cli` repository folder
///
/// Why don't write this script in Node instead running as command from Kotlin?
/// ...Well, why not?
fun runLineCountCLI(): Int {
  val workingDir = Environment.workingDir.resolve(NODE_JS_CLI_FOLDER)
  val coreDir = Environment.workingDir.resolve(CORE_MODULE_FOLDER)

  fun buildCorePackage() {
    runNpmShell(arrayOf(INSTALL_CLI), coreDir)
    runNpxShell(arrayOf(TYPESCRIPT_CLI), coreDir)
  }

  fun installNpmDependencies() {
    val commands = listOf(
      arrayOf(INSTALL_CLI, AS_DEV_DEPENDENCY, TYPESCRIPT_PACKAGE),
      arrayOf(INSTALL_CLI, AS_DEV_DEPENDENCY, TS_NODE_PACKAGE),
      arrayOf(INSTALL_CLI)
    )

    for (command in commands) {
      runNpmShell(arrayOf(*command), workingDir)
    }
  }

  buildCorePackage()
  installNpmDependencies()

  return runNodeShell(arrayOf(LINE_COUNT_CLI), workingDir).toInt()
}
