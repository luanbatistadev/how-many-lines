import com.lordcodes.turtle.shellRun
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.json.JSONObject
import org.json.JSONTokener
import io.lakscastro.howmanylines.utils.*
import java.io.File
import java.lang.System

/// Script that runs through the GitHub Action
/// after user login, this script will perform the following tasks:
/// 1. Fetch lines count using the `/cli` module
/// 2. Create a new PR add the name, profile picture, and line count to the `<login>.json` in the `pool` branch
/// 3. Note: If the data already, the file will be updated
suspend fun main() {
  /// You should provide these environment variables when
  /// running this script using the GitHub Action environment
  /// - `GITHUB_REPO_TOKEN` is the token linked to the repository (this token is the GitHub Action Bot Token)
  /// - `USER_TOKEN` is the token from the user that you want to get the stats (line count), OAuth required here
  /// - `REPO_NAME` is just the repository name, that will have the `README.md` updated on each new user
  /// - `WORKING_DIR` is the root path of the repository folder
  val repoToken = System.getenv(GITHUB_REPO_TOKEN_ENV)
  val token = System.getenv(USER_TOKEN_ENV)
  val repo = System.getenv(REPO_NAME_ENV)
  val workingDir = File(System.getenv(WORKING_DIR_ENV))

  val client = HttpClient(CIO)

  /// This script will
  val response = client.post<String>("$GITHUB_BASE_URL/user") {
    headers {
      append(AUTHORIZATION_HEADER, "Token $token")
    }
  }

  val user = JSONObject(JSONTokener(response))

  val avatarUrl = user.get(AVATAR_URL)
  val login = user.get(LOGIN)
  val profileUrl = user.get(HTML_URL)

  /// Why don't build this script in Node instead running as command from Kotlin? Well, why not?
  val result = shell(NODE, arrayOf(LINE_COUNT_CLI), workingDir.resolve(NODE_JS_CLI_FOLDER))

  print(result)
}
