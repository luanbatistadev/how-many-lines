package io.lakscastro.howmanylines.services

import java.io.File


/// `repository` refers to the complete String, like: `owner/repository`
/// `repo` refers only to the repository name `repository`
/// `owner` refers only to the owner login `owner`
object Environment {
  /// Environment variables constants
  private const val GITHUB_REPO_TOKEN_ENV = "GITHUB_REPO_TOKEN"
  private const val USER_TOKEN_ENV = "USER_TOKEN"
  private const val REPOSITORY_ENV = "REPOSITORY"
  private const val WORKING_DIR_ENV = "WORKING_DIR"

  private fun env(key: String) = System.getenv(key)

  val repositoryToken: String by lazy { env(GITHUB_REPO_TOKEN_ENV) }
  val token: String by lazy { env(USER_TOKEN_ENV) }
  val repository: String by lazy { env(REPOSITORY_ENV) }
  val workingDir: File by lazy { File(env(WORKING_DIR_ENV)) }

  /// Computed properties from `repo`environment variable
  val owner: String by lazy { repo.split("/")[0] }
  val repo: String by lazy { repo.split("/")[1] }
}