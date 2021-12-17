import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import org.json.JSONObject
import org.json.JSONTokener


const val GITHUB_REPO_TOKEN = "GITHUB_REPO_TOKEN"
const val USER_TOKEN = "USER_TOKEN"

suspend fun main() {
  val repoToken = System.getenv(GITHUB_REPO_TOKEN)
  val token = System.getenv(USER_TOKEN)

  val client = HttpClient(CIO)

  val response = client.post<String>("https://api.github.com/user") {
    headers {
      append("authorization", "Token $token")
    }
  }

  val user = JSONObject(JSONTokener(response))

  val avatarUrl = user.get("avatar_url")
  val login = user.get("login")
  val profileUrl = user.get("html_url")

  print(avatarUrl)
  print(login)
  print(profileUrl)
}
