package app

import auth.AuthController
import auth.FakeLoginForTestingController
import db.TransactionCoroutineContext
import domain.portfolios.PortfolioController
import io.jooby.Context
import io.jooby.Jooby
import io.jooby.Kooby
import io.jooby.require
import kotlinx.coroutines.slf4j.MDCContext

fun Kooby.registerRoutes() {
  coroutine {
    launchContext { it + MDCContext() + TransactionCoroutineContext() }

    mvc<HealthRoutes>()
    mvc<AuthController>()
    mvc<PortfolioController>()
    if (environment.isTest) mvc<FakeLoginForTestingController>()
  }
}

interface Before {
  fun before(ctx: Context)
}

inline fun <reified T: Any> Jooby.mvc() = require<T>().also {
  if (it is Before) before(it::before)
  mvc(it)
}
