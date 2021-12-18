package io.lakscastro.howmanylines

import io.lakscastro.howmanylines.services.*
import io.lakscastro.howmanylines.utils.*
import org.json.JSONObject

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
/// - `REPOSITORY` is just the repository with the owner and repo (example: "foo/bar")
/// - `WORKING_DIR` is the root path of the repository folder
suspend fun main() {
  val user = Github.authUser(Environment.token)
  val slug = Github.generateSlug(user)

  /// Try to create a label that will identify the user
  Github.createLabelIfNotExists(slug, Environment.repository, "Hey! This is your tag", Environment.repositoryToken)

  /// Try to create **the** label that will identify this kind of issues (Data pool issues)
  Github.createPoolLabel(Environment.repository, "Issues are just to hold data!", Environment.repositoryToken)

  val results = Github.searchIssueByLabels(Environment.repository, listOf(slug), Environment.repositoryToken)

  val firstTimeUser = results.isEmpty

  return if (firstTimeUser) createIssue(user) else updateIssue(user, results.first() as JSONObject)
}

/// Create issue for the first time users
suspend fun createIssue(user: JSONObject) {
  val lineCount = runLineCountCLI()

  val data = Github.generateIssuePoolData(user, lineCount)
  val slug = Github.generateSlug(user)

  Github.createPoolIssue(Environment.repo, Environment.owner, data, slug, Environment.repositoryToken)
}

/// Update issue for the non-first time users
fun updateIssue(user: JSONObject, issue: JSONObject) {
  val lineCount = runLineCountCLI()
  val data = Github.generateIssuePoolData(user, lineCount)

  /// TODO: Implement Github.updateIssue and  Github.updatePoolIssue
  /// Github.createPoolIssue(...)
}
