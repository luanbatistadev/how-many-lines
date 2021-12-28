package io.lakscastro.howmanylines.services

import io.lakscastro.howmanylines.interfaces.HttpClient
import io.lakscastro.howmanylines.interfaces.ResponseException
import io.lakscastro.howmanylines.markdown.MarkdownStats
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Github(private val client: HttpClient) {
  companion object {
    /// GitHub API User fields
    const val AVATAR_URL = "avatar_url"
    const val LOGIN = "login"

    /// GitHub API constants
    private const val GITHUB_BASE_URL = "https://api.github.com"
    private const val AUTHORIZATION_HEADER = "authorization"
    private const val LABEL_ALREADY_EXISTS_CODE = 422
    private const val RESOURCE_NOT_FOUND_CODE = 404
    private const val README_FILE = "README.md"

    /// Constant values related to the storage system based on issues
    const val POOL_ISSUE_LABEL = "t: pool"
    private const val USER_ISSUE_FIELD = "user"
    private const val STATS_ISSUE_FIELD = "stats"
    const val LINE_COUNT_ISSUE_FIELD = "line_count"
    private const val USER_ISSUE_LABEL_COLOR = "000000"
    private const val POOL_ISSUE_LABEL_COLOR = "003333"

    /// Implemented endpoints
    private const val CURRENT_USER_ENDPOINT = "/user"

    /// Repository Events
    const val OPEN_ISSUE_DISPATCH = "open-issue"
    const val BUILD_README_DISPATCH = "build-readme"
  }

  /// Make a generic `post` request using the `GITHUB_BASE_URL`
  private suspend fun post(url: String, token: String? = null, data: Map<String, Any>? = null): String {
    val result = client.post("$GITHUB_BASE_URL$url") {
      if (token != null) {
        headers = mapOf(AUTHORIZATION_HEADER to "Token $token")
      }
      if (data != null) {
        body = mapToJson(data)
      }
    }

    return result!!
  }

  /// Make a generic `post` request using the `GITHUB_BASE_URL`
  private suspend fun put(url: String, token: String? = null, data: Map<String, Any>? = null): String {
    val result = client.put("$GITHUB_BASE_URL$url") {
      if (token != null) {
        headers = mapOf(AUTHORIZATION_HEADER to "Token $token")
      }
      if (data != null) {
        body = mapToJson(data)
      }
    }

    return result!!
  }

  /// Make a generic `get` request using the `GITHUB_BASE_URL`
  private suspend fun get(url: String, token: String? = null): String {
    val response = client.get("$GITHUB_BASE_URL$url") {
      if (token != null) {
        headers = mapOf(AUTHORIZATION_HEADER to "Token $token")
      }
    }

    return response!!
  }

  /// Return the authenticated user given a `token`
  suspend fun authUser(token: String): JSONObject = parseJsonObject(get(CURRENT_USER_ENDPOINT, token))

  private suspend fun createLabel(
    name: String, repository: String, description: String, token: String, color: String = USER_ISSUE_LABEL_COLOR
  ): JSONObject {
    return parseJsonObject(
      post(
        "/repos/$repository/labels", token, mapOf("name" to name, "color" to color, "description" to description)
      ),
    )
  }

  /// Convenient method to call `createLabel` and ignore the exception when the label already exists
  suspend fun createLabelIfNotExists(
    name: String, repository: String, description: String, token: String, color: String = USER_ISSUE_LABEL_COLOR
  ) {
    try {
      /// Try to create a label that will identify the user
      createLabel(name, repository, description, token, color)
    } catch (e: ResponseException) {
      if (e.code == LABEL_ALREADY_EXISTS_CODE) {
        /// Ignore... the label is already created
      } else {
        throw e
      }
    }
  }

  /// Get the `README.md` of the repository, create if not exists
  suspend fun fetchReadme(repository: String, token: String, counter: Int = 5): JSONObject {
    val endpoint = "/repos/$repository/contents/$README_FILE"

    return try {
      parseJsonObject(get(endpoint, token))
    } catch (e: ResponseException) {
      if (counter > 0 && e.code == RESOURCE_NOT_FOUND_CODE) {
        put(endpoint, token, mapOf("message" to "Generate empty `README.md`", "content" to ""))

        fetchReadme(repository, token, counter - 1)
      } else {
        throw e
      }
    }
  }

  /// Get the `README.md` of the repository, create if not exists
  suspend fun fillReadme(repository: String, token: String, content: String, sha: String, message: String) {
    put(
      "/repos/$repository/contents/$README_FILE",
      token,
      mapOf(
        "message" to message,
        "content" to Base64.getEncoder().encodeToString(content.toByteArray()).trim(),
        "sha" to sha
      )
    )
  }

  /// Dispatch a given `event` to the given `repository`
  suspend fun dispatchEvent(
    repository: String,
    token: String,
    event: String,
    payload: Map<String, *> = emptyMap<String, String>()
  ) = post(
    "/repos/$repository/dispatches",
    token,
    mapOf("event_type" to event, "client_payload" to payload)
  )


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
        "/repos/$owner/$repo/issues", token, mapOf(
          "title" to "By `$slug`",
          "body" to "```json\n${mapToJson(data)}\n```",
          "assignees" to arrayOf(owner),
          "labels" to listOf(slug, POOL_ISSUE_LABEL)
        )
      ),
    )
  }

  /// Return a list of results, empty if it has no results
  suspend fun searchIssueByLabels(
    repository: String,
    labels: List<Any>,
    token: String,
    perPage: Int? = null,
    page: Int? = null,
  ): JSONArray {
    val query = mutableMapOf<String, String>()

    if (labels.isNotEmpty()) query["labels"] = labels.joinToString(",")
    if (page != null) query["page"] = "$page"
    if (perPage != null) query["per_page"] = "$perPage"

    val entries = query.entries.joinToString("&") { "${it.key}=${it.value}" }

    return parseJsonArray(get("/repos/$repository/issues?$entries", token))
  }

  /// This function accepts the object returned by `authUser` method or any User object from GitHub API
  fun generateSlug(user: JSONObject): String = "u: @${user.get(LOGIN)}"

  /// This function accepts the object returned by `authUser` method or any User object from GitHub API
  fun generateIssuePoolData(user: JSONObject, lineCount: Int): Map<String, Any> = mapOf(
    USER_ISSUE_FIELD to mapOf(
      LOGIN to user.get(LOGIN),
      AVATAR_URL to user.get(AVATAR_URL),
    ), STATS_ISSUE_FIELD to mapOf(
      LINE_COUNT_ISSUE_FIELD to lineCount
    )
  )

  private fun resolveUserFromPoolData(json: String): Map<String, Any> {
    val data = parseJsonObject(json)

    return mapOf(
      LOGIN to (data[USER_ISSUE_FIELD] as JSONObject)[LOGIN]!!,
      AVATAR_URL to (data[USER_ISSUE_FIELD] as JSONObject)[AVATAR_URL]!!,
    )
  }

  private fun resolveStatsFromPoolData(json: String): Map<String, Any> {
    val data = parseJsonObject(json)

    return mapOf(
      LINE_COUNT_ISSUE_FIELD to (data[STATS_ISSUE_FIELD] as JSONObject)[LINE_COUNT_ISSUE_FIELD]!!,
    )
  }

  fun resolveMarkdownStatsFromPoolData(json: String): MarkdownStats {
    val user = resolveUserFromPoolData(json)
    val stats = resolveStatsFromPoolData(json)

    return MarkdownStats(user[LOGIN] as String, user[AVATAR_URL] as String, stats[LINE_COUNT_ISSUE_FIELD] as Int)
  }
}