package io.lakscastro.howmanylines.utils

import com.lordcodes.turtle.shellRun
import io.lakscastro.howmanylines.services.Environment
import java.io.File

/// OS Executables
const val CMD = "cmd.exe"
const val BASH = "bash"
const val NODE = "node"
const val NPM = "npm"
const val NPX = "npx"

/// Executable constants
const val LINE_COUNT_CLI = "line-count.js"
const val INSTALL_CLI = "install"
const val TYPESCRIPT_CLI = "tsc"
const val CMD_COMMAND = "/c"
const val BASH_COMMAND = "-c"
const val AS_DEV_DEPENDENCY = "-D"

/// Typescript/Javascript packages
const val TYPESCRIPT_PACKAGE = "typescript"
const val TS_NODE_PACKAGE = "ts-node"

/// Script constants
const val NODE_JS_CLI_FOLDER = "../cli"
const val CORE_MODULE_FOLDER = "../core"

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
private fun shell(command: String, arguments: Array<String>, workingDir: File): String {
  return when(os) {
    OS.WINDOWS -> shellRun(CMD, listOf(CMD_COMMAND, command, *arguments), workingDir)
    OS.LINUX -> shellRun(BASH, listOf(BASH_COMMAND, "$command ${arguments.joinToString(" ")}"), workingDir)
    else -> throw UnsupportedOperationException("We can't handle this OS: $os")
  }
}

/// Run CLI command as `node <arguments>` in a given `workingDir`
private fun runNodeShell(arguments: Array<String>, workingDir: File): String = shell(NODE, arguments, workingDir)

/// Run CLI command as `npm <arguments>` in a given `workingDir`
private fun runNpmShell(arguments: Array<String>, workingDir: File): String = shell(NPM, arguments, workingDir)

/// Run CLI command as `npx <arguments>` in a given `workingDir`
private fun runNpxShell(arguments: Array<String>, workingDir: File): String = shell(NPX, arguments, workingDir)

/// Execute the CLI module that can be found in `~/cli` repository folder
///
/// Why don't write this script in Node instead running as command from Kotlin?
/// ...Well, why not?
fun runLineCountCLI(): Int {
  val workingDir = Environment.workingDir.resolve(NODE_JS_CLI_FOLDER)
  val coreDir = Environment.workingDir.resolve(CORE_MODULE_FOLDER)

  fun buildCorePackage() {
    runNpmShell(arrayOf(INSTALL_CLI), coreDir);
    runNpxShell(arrayOf(TYPESCRIPT_CLI), coreDir);
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

  buildCorePackage();
  installNpmDependencies()

  return runNodeShell(arrayOf(LINE_COUNT_CLI), workingDir).toInt()
}
