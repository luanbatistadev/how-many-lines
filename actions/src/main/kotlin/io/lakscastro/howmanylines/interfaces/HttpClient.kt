package io.lakscastro.howmanylines.interfaces

data class Request(var headers: Map<String, String>? = null, var body: String? = null)

data class ResponseException(val code: Int, val error: Any) : Exception("Error: $error, code: $code")

/// Simple and minimal required interface for this project of an `HttpClient`
abstract class HttpClient {
  abstract suspend fun get(url: String, block: Request.() -> Unit): String?
  abstract suspend fun post(url: String, block: Request.() -> Unit): String?
  abstract suspend fun put(url: String, block: Request.() -> Unit): String?
}
