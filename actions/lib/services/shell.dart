import 'dart:io';

import 'package:path/path.dart' as path;

/// OS Executables
const kNode = 'node';
const kNpm = 'npm';
const kNpx = 'npx';

/// Executable constants
const kLineCountCLI = 'line-count.js';
const kInstallCLI = 'install';

/// Script constants
const kNodeCLI = 'cli';
const kCorePackage = 'core';

Future<ProcessResult> shell(String command,
    {List<String> arguments = const [], required String workingDir}) async {
  final result = await Process.run(command, arguments,
      workingDirectory: workingDir, runInShell: true);

  if (result.exitCode != 0) {
    print(
        'We got an error when running: $command ${arguments.join(' ')}\nError: ${result.stderr}');

    throw result;
  }

  return result;
}

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
Future<ProcessResult> runNodeShell(
        {List<String> arguments = const [], required String workingDir}) =>
    shell(kNode, arguments: arguments, workingDir: workingDir);

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
Future<ProcessResult> runNpmShell(
        {List<String> arguments = const [], required String workingDir}) =>
    shell(kNpm, arguments: arguments, workingDir: workingDir);

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
Future<ProcessResult> runNpxShell(
        {List<String> arguments = const [], required String workingDir}) =>
    shell(kNpx, arguments: arguments, workingDir: workingDir);

/// Execute the CLI module that can be found in `~/cli` repository folder
///
/// Why don't write this script in Node instead running as command from Kotlin?
/// ...Well, why not?
Future<int> runLineCountCLI() async {
  final currentDir = Directory.current;

  final workingDir = path.normalize(path.join(currentDir.path, '..', kNodeCLI));
  final coreDir =
      path.normalize(path.join(currentDir.path, '..', kCorePackage));

  Future<void> buildCorePackage() async {
    await runNpmShell(arguments: ['install'], workingDir: coreDir);
    await runNpxShell(arguments: ['tsc'], workingDir: coreDir);
  }

  Future<void> installNpmDependencies() async {
    final tasks = <List<String>>[
      [kInstallCLI, '-D', 'typescript'],
      [kInstallCLI, '-D', 'ts-node'],
      [kInstallCLI],
    ];

    for (final args in tasks) {
      await runNpmShell(arguments: args, workingDir: workingDir);
    }
  }

  await buildCorePackage();
  await installNpmDependencies();

  final result =
      await runNodeShell(arguments: [kLineCountCLI], workingDir: workingDir);

  try {
    return result.exitCode;
  } on FormatException catch (e) {
    print(
        'Expected a `int` but got source: ${e.source} | offset: ${e.offset} | message: ${e.message}');

    rethrow;
  }
}
