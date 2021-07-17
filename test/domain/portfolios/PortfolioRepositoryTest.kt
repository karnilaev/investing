package domain.portfolios

import auth.HashingService
import auth.Role
import auth.User
import auth.UserRepository
import db.DBTest.Companion.db
import db.exec
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PortfolioRepositoryTest {
  private val repository = PortfolioRepository(db)
  private val userRepository = UserRepository(db, HashingService(), mockk(relaxed = true))
  private lateinit var user1: User
  private lateinit var user2: User
  private lateinit var user3: User

  @BeforeEach
  internal fun emptyPortfolioTable() {
    db.exec("delete from portfolios")
    db.exec("delete from users where login != 'admin'")
    user1 = user("user1")
    user2 = user("user2")
    user3 = user("user3")
  }

  @Test
  internal fun createAndGet() {
    val portfolio = Portfolio(name = "portfolio")
    val savedPortfolio = repository.create(portfolio, user1)
    assertThat(savedPortfolio.id).isEqualTo(portfolio.id)
    assertThat(savedPortfolio.name).isEqualTo("portfolio")

    val receivedPortfolio = repository.findById(portfolio.id, user1)
    assertThat(receivedPortfolio).isNotNull
    assertThat(receivedPortfolio).isEqualTo(portfolio)

    val emptyResult = repository.findById(portfolio.id, user2)
    assertThat(emptyResult).isNull()
  }

  @Test
  internal fun updatePortfolio() {
    val portfolioBeforeUpdate = Portfolio(name = "portfolio")
      .also { repository.create(it, user1) }
      .let { repository.findById(it.id, user1) }!!

    assertThat(portfolioBeforeUpdate.name).isEqualTo("portfolio")

    val portfolioAfterUpdate = Portfolio(id = portfolioBeforeUpdate.id, name = "new name")
      .also { repository.update(it, user1) }
      .let { repository.findById(portfolioBeforeUpdate.id, user1) }!!

    assertThat(portfolioAfterUpdate.name).isEqualTo("new name")
  }

  @Test
  internal fun updatePortfolioByAnotherUser() {
    val portfolio = Portfolio(name = "portfolio")
      .also { repository.create(it, user1) }
      .copy(name = "ne name")

    assertThrows<NoSuchElementException> { repository.update(portfolio, user2) }
  }

  @Test
  internal fun findAll() {
    val portfolio1 = Portfolio(name = "portfolio1").also { repository.create(it, user1) }
    val portfolio2 = Portfolio(name = "portfolio2").also { repository.create(it, user1) }
    val portfolio3 = Portfolio(name = "portfolio3").also { repository.create(it, user2) }

    assertThat(repository.listAll(user1)).hasSize(2)
    assertThat(repository.listAll(user1)).hasSameElementsAs(listOf(portfolio1, portfolio2))
    assertThat(repository.listAll(user2)).hasSize(1)
    assertThat(repository.listAll(user2)).hasSameElementsAs(listOf(portfolio3))
    assertThat(repository.listAll(user3)).hasSize(0)
  }

  private fun user(login: String): User {
    return userRepository.create(login, Role.USER, "en", "123")
  }
}