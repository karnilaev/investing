package domain.portfolios

import auth.Access
import auth.Role
import auth.User
import io.jooby.Context
import io.jooby.annotations.GET
import io.jooby.annotations.POST
import io.jooby.annotations.PathParam
import io.jooby.exception.UnauthorizedException
import java.util.*

class PortfolioController(private val repository: PortfolioRepository) {

  @GET("/portfolios/list") @Access(Role.USER)
  fun listAll(ctx: Context): List<Portfolio> =
    repository.listAll(user(ctx))

  @GET("/portfolios/{id}")
  fun findById(@PathParam id: String, ctx: Context): Portfolio =
    repository.findById(UUID.fromString(id), user(ctx)) ?: throw NoSuchElementException()

  @POST("/portfolios")
  fun save(@PathParam portfolio: Portfolio, ctx: Context): Portfolio =
    repository.save(portfolio, user(ctx))

  private fun user(ctx: Context): User =
    ctx.getUser() ?: throw UnauthorizedException("login.unauthorized")

}
