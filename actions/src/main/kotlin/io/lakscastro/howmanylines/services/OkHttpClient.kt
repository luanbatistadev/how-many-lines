package io.lakscastro.howmanylines.services

import io.lakscastro.howmanylines.interfaces.HttpClient
import io.lakscastro.howmanylines.interfaces.Request
import io.lakscastro.howmanylines.interfaces.ResponseException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Request as OkHttpRequest

/// `HttpClient` implementation using `OkHttp` library with minimal required features
class OkHttpClient : HttpClient() {
  companion object {
    val client by lazy { OkHttpClient() }

    val MEDIA_TYPE_PLAIN_TEXT = "text/plain; charset=utf-8".toMediaType()
  }

  override suspend fun get(url: String, block: Request.() -> Unit): String? {
    val config = Request().apply { block(this) }

    val request = OkHttpRequest.Builder().url(url)

    addHeadersTo(request, config.headers)

    return executeRequest(request)
  }

  override suspend fun post(url: String, block: Request.() -> Unit): String? {
    val config = Request().apply { block(this) }

    val body = config.body ?: ""

    val request = OkHttpRequest.Builder().url(url).apply {
      addHeadersTo(this, config.headers)

      post(body.toRequestBody(MEDIA_TYPE_PLAIN_TEXT))
    }

    return executeRequest(request)
  }

  override suspend fun put(url: String, block: Request.() -> Unit): String? {
    val config = Request().apply { block(this) }

    val body = config.body ?: ""

    val request = OkHttpRequest.Builder().url(url).apply {
      addHeadersTo(this, config.headers)

      put(body.toRequestBody(MEDIA_TYPE_PLAIN_TEXT))
    }

    return executeRequest(request)
  }

  private fun executeRequest(request: OkHttpRequest.Builder): String? {
    val response = client.newCall(request.build()).execute()

    if (!response.isSuccessful) {
      throw ResponseException(response.code, response)
    }

    return response.body?.string()
  }

  private fun addHeadersTo(request: OkHttpRequest.Builder, headers: Map<String, String>?) {
    for (key in headers?.keys ?: emptySet()) {
      request.addHeader(key, headers!![key]!!)
    }
  }
}