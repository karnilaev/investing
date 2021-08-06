package util

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.Duration.ofSeconds
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.failedFuture

class JsonHttpClientTest {
  val httpClient = mockk<HttpClient>()
  val http = JsonHttpClient("http://some.host/v1", reqModifier = { setHeader("X-Custom-API", "123") },
    retryCount = 2, retryAfter = ofSeconds(0), http = httpClient)

  @Test
  fun get() {
    val response = mockResponse(200, """{"hello": "World"}""")
    every { httpClient.sendAsync<String>(any(), any()) } returns completedFuture(response)

    runBlocking {
      val data = http.get<SomeData>("/some/data")
      assertThat(data).isEqualTo(SomeData("World"))
    }

    coVerify { httpClient.sendAsync<String>(match { it.uri().toString() == "http://some.host/v1/some/data" }, any()) }
  }

  @Test
  fun `http error`() {
    val response = mockResponse(500, """{"error": "Error"}""")
    every { httpClient.sendAsync<String>(any(), any()) } returns completedFuture(response)

    assertThrows<IOException> { runBlocking { http.get<SomeData>("/error") } }
  }

  @Test
  fun exception() {
    val exception = IOException()
    every { httpClient.sendAsync<String>(any(), any()) }.returnsMany(failedFuture(exception))
    assertThrows<IOException> { runBlocking { http.post<String>("/some/data", "Hello") } }
    coVerify(exactly = 3) { httpClient.sendAsync<String>(any(), any()) }
  }

  @Test
  fun retry() {
    val response = mockResponse(200, """{"hello": "World"}""")
    every { httpClient.sendAsync<String>(any(), any()) }.returnsMany(failedFuture(IOException()), completedFuture(response))
    runBlocking {
      val body = http.post<String>("/some/data", "Hello")
      assertThat(body).isEqualTo(response.body())
    }
    coVerify(exactly = 2) { httpClient.sendAsync<String>(any(), any()) }
  }

  data class SomeData(val hello: String)

  private fun mockResponse(status: Int, body: String) = mockk<HttpResponse<String>> {
    every { statusCode() } returns status
    every { body() } returns body
  }
}