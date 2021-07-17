package domain.portfolios

import auth.User
import db.*
import java.sql.ResultSet
import java.util.*
import javax.sql.DataSource

class PortfolioRepository(db: DataSource): BaseRepository(db, "portfolios") {
  private val mapper: ResultSet.() -> Portfolio = {
    Portfolio(getId(), getString("name"))
  }

  fun listAll(user: User): List<Portfolio> =
    db.query(table, mapOf("user_id" to user.id), mapper = mapper)

  fun findById(id: UUID, user: User): Portfolio? =
    db.query(table, mapOf("id" to id, "user_id" to user.id), mapper = mapper).firstOrNull()

  fun create(portfolio: Portfolio, user: User): Portfolio = portfolio.also {
    db.insert(table, mapOf(
      "id" to portfolio.id,
      "user_id" to user.id,
      "name" to portfolio.name
    ))
  }

  fun update(portfolio: Portfolio, user: User): Portfolio = portfolio.also {
    db.update(table,
      mapOf(
        "id" to portfolio.id,
        "user_id" to user.id
      ),
      mapOf(
        "name" to portfolio.name
      )).also {
      if (it == 0) {
        throw NoSuchElementException("portfolio with id=${portfolio.id} and user=${user.login} is absent")
      }
    }
  }

}
