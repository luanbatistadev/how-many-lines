import 'dart:io';

import 'package:path/path.dart' as path;

/// OS Executables
const kNode = 'node';
const kNpm = 'npm';

/// Executable constants
const kLineCountCLI = 'line-count.js';
const kInstallCLI = 'install';

/// Script constants
const kNodeCLI = 'cli/';

Future<String> shell(String command,
    {List<String> arguments = const [], required String workingDir}) async {
  final result = await Process.run(command, arguments,
      workingDirectory: workingDir, runInShell: true);

  return result.stdout;
}

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
Future<String> runNodeShell(
        {List<String> arguments = const [], required String workingDir}) =>
    shell(kNode, arguments: arguments, workingDir: workingDir);

/// `command` isn't the `cmd.exe` or `bash`
/// but the executable that you need to run like `node` or `pip`
Future<String> runNpmShell(
        {List<String> arguments = const [], required String workingDir}) =>
    shell(kNpm, arguments: arguments, workingDir: workingDir);

/// Execute the CLI module that can be found in `~/cli` repository folder
///
/// Why don't write this script in Node instead running as command from Kotlin?
/// ...Well, why not?
Future<int> runLineCountCLI() async {
  final currentDir = Directory.current;

  final workingDir = path.normalize(path.join(currentDir.path, '..', kNodeCLI));

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

  await installNpmDependencies();

  final result =
      await runNodeShell(arguments: [kLineCountCLI], workingDir: workingDir);

  try {
    return int.parse(result);
  } on FormatException catch (e) {
    print(
        'Expected a `int` but got source: ${e.source} | offset: ${e.offset} | message: ${e.message}');

    rethrow;
  }
}
