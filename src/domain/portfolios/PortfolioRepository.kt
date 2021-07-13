package domain.portfolios

import auth.User
import java.util.*

class PortfolioRepository {

  fun listAll(user: User): List<Portfolio> {
    return emptyList()
  }

  fun findById(id: UUID, user: User): Portfolio? {
    return null
  }

  fun save(portfolio: Portfolio, user: User): Portfolio {
    return portfolio
  }

}
