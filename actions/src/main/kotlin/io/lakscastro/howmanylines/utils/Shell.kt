package io.lakscastro.howmanylines.utils

import com.lordcodes.turtle.shellRun
import java.io.File

/// OS Executables
const val CMD = "cmd.exe"
const val BASH = "bash"
const val NODE = "node"

/// Executable constants
const val LINE_COUNT_CLI = "line-count.js"
const val CMD_COMMAND = "/c"
const val BASH_COMMAND = "-c"

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
fun shell(command: String, arguments: Array<String>, workingDir: File) {
  val executable = if (os.isWindows()) CMD else BASH
  val arg = if (os.isWindows()) CMD_COMMAND else BASH_COMMAND

  shellRun(executable, listOf(arg, command, *arguments), workingDir)
}
