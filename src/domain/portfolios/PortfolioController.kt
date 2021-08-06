package domain.portfolios

import auth.Access
import auth.Role
import auth.User
import io.jooby.Context
import io.jooby.annotations.*
import io.jooby.exception.UnauthorizedException
import java.util.*

@Path("/portfolios")
class PortfolioController(private val portfolioRepository: PortfolioRepository) {

  @GET @Access(Role.USER)
  fun listAll(ctx: Context): List<Portfolio> =
    portfolioRepository.listAll(user(ctx))

  @GET("/{id}")
  fun findById(@PathParam id: String, ctx: Context): Portfolio =
    portfolioRepository.findById(UUID.fromString(id), user(ctx)) ?: throw NoSuchElementException()

  @POST
  fun create(portfolio: Portfolio, ctx: Context): Portfolio =
    portfolioRepository.create(portfolio, user(ctx))

  @PUT
  fun update(portfolio: Portfolio, ctx: Context): Portfolio =
    portfolioRepository.update(portfolio, user(ctx))

  private fun user(ctx: Context): User =
    ctx.getUser() ?: throw UnauthorizedException("login.unauthorized")

}
