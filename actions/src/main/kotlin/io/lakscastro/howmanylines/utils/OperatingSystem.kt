package io.lakscastro.howmanylines.utils

/// System properties constants
const val OS_NAME_PROPERTY = "os.name"
const val DEFAULT_OS_NAME = "Unknown"

/// OS Prefix constants
const val WINDOWS_PREFIX = "win"
const val MACOS_PREFIX = "mac"
const val DARWIN_PREFIX = "darwin"
const val UNIX_PREFIX = "nux"

/// cached result of OS detection
private var detectedOS: OS? = null

val os: OS
  get() {
    if (detectedOS == null) {
      val os = System.getProperty(OS_NAME_PROPERTY, DEFAULT_OS_NAME).lowercase()

      detectedOS = if (os.indexOf(MACOS_PREFIX) >= 0 || os.indexOf(DARWIN_PREFIX) >= 0) {
        OS.MACOS
      } else if (os.indexOf(WINDOWS_PREFIX) >= 0) {
        OS.WINDOWS
      } else if (os.indexOf(UNIX_PREFIX) >= 0) {
        OS.LINUX
      } else {
        OS.OTHER
      }
    }

    return detectedOS!!
  }

/// Available Operating Systems
enum class OS {
  WINDOWS, MACOS, LINUX, OTHER
}

/// Convenient methods
fun OS.isWindows() = this == OS.WINDOWS
fun OS.isMacOS() = this == OS.MACOS
fun OS.isLinux() = this == OS.LINUX
fun OS.isUnknown() = this == OS.OTHER
