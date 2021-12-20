import './services/environment.dart' as environment;
import './services/github.dart' as github;
import './services/shell.dart' as shell;

/// Script that runs through the GitHub Action
/// after user login, this script will perform the following tasks:
/// 1. Fetch lines count using the `/cli` module
/// 2. Create a new PR add the name, profile picture, and line count to the `<login>.json` in the `pool` branch
/// 3. Note: If the data already, the file will be updated
///
/// Be aware that you should provide these environment variables when
/// running this script using the GitHub Action environment
/// - `GITHUB_REPO_TOKEN` is the token linked to the repository (this token is the GitHub Action Bot Token)
/// - `USER_TOKEN` is the token from the user that you want to get the stats (line count), OAuth required here
/// - `REPOSITORY` is just the repository with the owner and repo (example: 'foo/bar')
Future<void> main() async {
  final user = await github.authUser(environment.token);
  final slug = github.generateSlug(user);

  /// Try to create a label that will identify the user
  await github.createLabelIfNotExists(
      name: slug,
      repository: environment.repository,
      description: 'Hey! This is your tag',
      repoToken: environment.repositoryToken);

  /// Try to create **the** label that will identify this kind of issues (Data pool issues)
  await github.createPoolLabel(
      repository: environment.repository,
      description: 'Issues are just to hold data!',
      repoToken: environment.repositoryToken);

  final results = await github.searchIssueByLabels(
      repository: environment.repository,
      labels: [slug],
      token: environment.repositoryToken);

  final firstTimeUser = results.isEmpty;

  if (firstTimeUser) {
    await createIssue(user);
  } else {
    await updateIssue(user, results.first);
  }
}

/// Create issue for the first time users
Future<void> createIssue(Map<String, dynamic> user) async {
  final lineCount = await shell.runLineCountCLI();

  final data = github.generateIssuePoolData(user, lineCount);
  final slug = github.generateSlug(user);

  github.createPoolIssue(
      repo: environment.repo,
      owner: environment.owner,
      data: data,
      slug: slug,
      repoToken: environment.repositoryToken);
}

/// Update issue for the non-first time users
Future<void> updateIssue(
    Map<String, dynamic> user, Map<String, dynamic> issue) async {
  final lineCount = await shell.runLineCountCLI();
  final data = github.generateIssuePoolData(user, lineCount);

  /// TODO: Implement github.updateIssue and  github.updatePoolIssue
  /// github.createPoolIssue(...)
}
