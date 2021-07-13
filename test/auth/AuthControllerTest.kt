package auth

import app.TestData
import io.jooby.Context
import io.jooby.Session
import io.jooby.exception.UnauthorizedException
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuthControllerTest {
  private val user = TestData.user
  private val session = mockk<Session>(relaxed = true)
  private val ctx = mockk<Context>(relaxed = true)
  private val userRepository = mockk<UserRepository>(relaxed = true) {
    every { create(any(), any(), any(), any()) } returns user
  }
  private val controller = AuthController(userRepository)

  @Test
  fun `login with password`() {
    every { userRepository.byCredentials("admin", "secret") } returns user
    assertThat(controller.login(AuthRequest("admin", "secret"), session, ctx)).isEqualTo(user)
    verifyOrder { sessionInitialized() }
  }

  @Test
  fun `invalid auth`() {
    every { userRepository.byCredentials(any(), any()) } returns null
    assertThrows<UnauthorizedException> { controller.login(AuthRequest("foo", "bar"), session, ctx) }
    verify { session.clear() }
    verify(exactly = 0) { session.put("userId", any<String>()) }
  }

  private fun MockKVerificationScope.sessionInitialized() {
    session.apply {
      clear()
      put("userId", user.id.toString())
      put("loginTime", any<Long>())
    }
    ctx.setUser(match<User> { it.id == user.id })
  }

  @Test
  fun logout() {
    controller.logout(session, ctx)

    verify {
      session.clear()
      ctx.sendRedirect("/")
    }
  }
}
