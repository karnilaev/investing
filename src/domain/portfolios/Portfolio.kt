package domain.portfolios

import db.BaseModel
import java.util.*

data class Portfolio(
  override val id: UUID = UUID.randomUUID(),
  val name: String
): BaseModel
