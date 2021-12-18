package io.lakscastro.howmanylines.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.lakscastro.howmanylines.utils.*
import org.json.JSONArray
import org.json.JSONObject


object Github {
  /// GitHub API User fields
  private const val AVATAR_URL = "avatar_url"
  private const val LOGIN = "login"
  private const val HTML_URL = "html_url"

  /// GitHub API constants
  private const val GITHUB_BASE_URL = "https://api.github.com"
  private const val AUTHORIZATION_HEADER = "authorization"
  private const val LABEL_ALREADY_EXISTS_CODE = 422

  /// Constant values related to the storage system based on issues
  private const val POOL_ISSUE_LABEL = "t: Pool Issue"
  private const val USER_ISSUE_FIELD = "user"
  private const val STATS_ISSUE_FIELD = "stats"
  private const val LINE_COUNT_ISSUE_FIELD = "lineCount"
  private const val USER_ISSUE_LABEL_COLOR = "000000"
  private const val POOL_ISSUE_LABEL_COLOR = "003333"

  /// HTTP Client from `ktor` library
  private val client = HttpClient(CIO)

  /// Implemented endpoints
  private const val CURRENT_USER_ENDPOINT = "/user"

  /// Make a generic `post` request using the `GITHUB_BASE_URL`
  private suspend fun post(url: String, token: String? = null, data: Map<String, Any>? = null): String {
    val response = client.post<String>("$GITHUB_BASE_URL$url") {
      headers {
        if (token != null) {
          append(AUTHORIZATION_HEADER, "Token $token")
        }
      }
      if (data != null) body = mapToJson(data)
    }

    return response
  }

  /// Make a generic `get` request using the `GITHUB_BASE_URL`
  private suspend fun get(url: String, token: String? = null): String {
    val response = client.get<String>("$GITHUB_BASE_URL$url") {
      headers {
        if (token != null) {
          append(AUTHORIZATION_HEADER, "Token $token")
        }
      }
    }

    return response
  }

  /// Return the authenticated user given a `token`
  suspend fun authUser(token: String): JSONObject = parseJsonObject(post(CURRENT_USER_ENDPOINT, token))

  private suspend fun createLabel(
    name: String,
    repository: String,
    description: String,
    token: String,
    color: String = USER_ISSUE_LABEL_COLOR
  ): JSONObject {
    return parseJsonObject(
      post(
        "/repos/$repository/labels",
        token,
        mapOf("name" to name, "color" to color, "description" to description)
      ),
    )
  }

  /// Convenient method to call `createLabel` and ignore the exception when the label already exists
  suspend fun createLabelIfNotExists(
    name: String,
    repository: String,
    description: String,
    token: String,
    color: String = USER_ISSUE_LABEL_COLOR
  ) {
    try {
      /// Try to create a label that will identify the user
      createLabel(name, repository, description, token, color)
    } catch (e: ClientRequestException) {
      if (e.response.status.value == LABEL_ALREADY_EXISTS_CODE) {
        /// Ignore... the label is already created
      } else {
        throw e
      }
    }
  }

  suspend fun createPoolLabel(repository: String, description: String, repositoryToken: String) {
    return createLabelIfNotExists(POOL_ISSUE_LABEL, repository, description, repositoryToken, POOL_ISSUE_LABEL_COLOR)
  }

  /// Create a new issue in the repository given the arguments, directly call the GitHub API
  suspend fun createIssue(
    title: String,
    repository: String,
    content: String,
    labels: List<Any>,
    assignees: List<String>,
    token: String,
  ): JSONObject {
    return parseJsonObject(
      post(
        "/repos/$repository/issues",
        token,
        mapOf("title" to title, "body" to content, "assignees" to assignees, "labels" to labels)
      ),
    )
  }

  /// Create a pool issue (abstraction of `createIssue` method)
  suspend fun createPoolIssue(
    repo: String,
    owner: String,
    data: Map<String, Any>,
    slug: String,
    token: String,
  ): JSONObject {
    return parseJsonObject(
      post(
        "/repos/$owner/$repo/issues",
        token,
        mapOf(
          "title" to "To $slug",
          "body" to mapToJson(data),
          "assignees" to owner,
          "labels" to listOf(slug, POOL_ISSUE_LABEL)
        )
      ),
    )
  }

  /// Return a list of results, empty if it has no results
  suspend fun searchIssueByLabels(repository: String, labels: List<Any>, token: String): JSONArray {
    return parseJsonArray(get("/repos/$repository/issues?labels=${labels.joinToString(",")}", token))
  }

  /// This function accepts the object returned by `authUser` method or any User object from GitHub API
  fun generateSlug(user: JSONObject): String = "u: @${user.get(LOGIN)}"

  /// This function accepts the object returned by `authUser` method or any User object from GitHub API
  fun generateIssuePoolData(user: JSONObject, lineCount: Int): Map<String, Any> =
    mapOf(
      USER_ISSUE_FIELD to mapOf(
        LOGIN to user.get(LOGIN),
        AVATAR_URL to user.get(AVATAR_URL),
        HTML_URL to user.get(AVATAR_URL)
      ),
      STATS_ISSUE_FIELD to mapOf(
        LINE_COUNT_ISSUE_FIELD to lineCount
      )
    )
}