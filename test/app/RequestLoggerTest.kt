package app

import auth.User
import io.jooby.Context
import io.jooby.Route
import io.jooby.StatusCode.ACCEPTED
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.MDC

class RequestLoggerTest {
  private val ctx = mockk<Context>(relaxed = true) {
    every { remoteAddress } returns "127.0.0.13"
    every { method } returns "GET"
    every { requestPath } returns "/path"
    every { attributes } returns emptyMap()
    every { queryString() } returns "?q=hello"
    every { header(any()).valueOrNull() } returns null
    every { header("Referer").value("") } returns "http://referrer"
    every { header("User-Agent").value("") } returns "User-Agent"
    every { responseLength } returns 12345
    every { responseCode } returns ACCEPTED
    every { getUser<User>() } returns TestData.user
  }

  private val requestLog = mockk<Logger>(relaxed = true)
  private val handler = RequestLogger(requestLog)

  @Test
  fun `successful request log with proxy`() {
    every { ctx.header("X-Request-Id").valueOrNull() } returns "r-id"
    handler.logOnComplete(ctx)
    assertThat(MDC.get("requestId")).endsWith("/r-id")
    runCompleteHandler()

    val user = TestData.user
    verify {
      requestLog.info(match {
        it.matches(
          """USER:${user.id} "GET /path\?q=hello" 202 12345 \d+ ms http://referrer "User-Agent"""".toRegex()
        )
      })
    }
    assertThat(MDC.get("requestId")).isNull()
  }

  private fun runCompleteHandler() {
    val completeHandler = slot<Route.Complete>()
    verify { ctx.onComplete(capture(completeHandler)) }
    completeHandler.captured.apply(ctx)
  }
}
