package domain.portfolios

import app.TestData.portfolio
import app.TestData.user
import auth.User
import io.jooby.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

internal class PortfolioControllerTest {
  private val existsPortfolioUUID = UUID.randomUUID()
  private val absentPortfolioUUID = UUID.randomUUID()
  private val portfolioRepository = mockk<PortfolioRepository>(relaxed = true) {
    every { listAll(any()) } returns listOf(portfolio)
    every { findById(existsPortfolioUUID, user) } returns portfolio
    every { findById(absentPortfolioUUID, user) } returns null
    every { save(portfolio, user) } returns portfolio
  }
  private val ctx = mockk<Context>(relaxed = true) {
    every { getUser<User>() } returns user
  }
  private val controller = PortfolioController(portfolioRepository)

  @Test
  internal fun `list of all portfolios`() {
    assertThat(controller.listAll(ctx)).isEqualTo(listOf(portfolio))
    verify { portfolioRepository.listAll(any()) }
  }

  @Test
  internal fun `portfolio by id`() {
    assertThat(controller.findById(existsPortfolioUUID.toString(), ctx)).isEqualTo(portfolio)
    verify { portfolioRepository.findById(existsPortfolioUUID, user) }
  }

  @Test
  internal fun `portfolio by id is absent`() {
    assertThrows<NoSuchElementException> { controller.findById(absentPortfolioUUID.toString(), ctx) }
    verify { portfolioRepository.findById(absentPortfolioUUID, user) }
  }

  @Test
  internal fun `save portfolio`() {
    assertThat(controller.save(portfolio, ctx)).isEqualTo(portfolio)
    verify { portfolioRepository.save(portfolio, user) }
  }
}