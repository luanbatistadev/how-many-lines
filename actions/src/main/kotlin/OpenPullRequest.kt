import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.json.JSONObject
import org.json.JSONTokener

const val GITHUB_REPO_TOKEN = "GITHUB_REPO_TOKEN"
const val USER_TOKEN = "USER_TOKEN"
const val REPO_NAME = "REPO_NAME"

const val AVATAR_URL = "avatar_url"
const val LOGIN = "login"
const val HTML_URL = "html_url"

/// Script that runs through the Github Action
/// after user login, this script will perform the following tasks:
/// 1. Fetch lines count using the `/cli` module
/// 2. Create a new PR add the name, profile picture, and line count to the `<login>.json` in the `pool` branch
/// 3. Note: If the data already, the file will be updated
suspend fun main() {
  /// You should provide these environment variables when
  /// running this script using the Github Action environment
  /// - `GITHUB_REPO_TOKEN` is the token linked to the repository (this token is the Github Action Bot Token)
  /// - `USER_TOKEN` is the token from the user that you want to get the stats (line count), OAuth required here
  /// - `REPO_NAME` is just the repository name, that will have the `README.md` updated on each new user
  val repoToken = System.getenv(GITHUB_REPO_TOKEN)
  val token = System.getenv(USER_TOKEN)
  val repo = System.getenv(REPO_NAME)

  val client = HttpClient(CIO)

  /// Fetch data from the current logged user to display in the `README.md` table
  /// Note: the data will be only displayed after `<!-- START USER STATISTICS SECTION --!>` and before the `<!-- END USER STATISTICS SECTION --!>`
  val response = client.post<String>("https://api.github.com/user") {
    headers {
      append("authorization", "Token $token")
    }
  }

  val user = JSONObject(JSONTokener(response))

  val avatarUrl = user.get(AVATAR_URL)
  val login = user.get(LOGIN)
  val profileUrl = user.get(HTML_URL)

  print(avatarUrl)
  print(login)
  print(profileUrl)

  /// TODO: Create a pool branch following the data structure
  /// <login>.json
}