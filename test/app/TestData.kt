package app

import auth.Role.USER
import auth.User
import domain.portfolios.Portfolio

/** Reusable entities to use in tests to avoid specifying all params */
object TestData {
  val user = User(login = "login", role = USER, lang = "en")
  val portfolio = Portfolio(name = "portfolio name")
}
