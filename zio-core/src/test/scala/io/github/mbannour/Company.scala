package io.github.mbannour

import org.mongodb.scala.bson.ObjectId

case class Company(
    _id: ObjectId,
    name: String,
    category_code: String,
    founded_year: Int,
    description: String,
    funding_rounds: List[FundingRound]
)
case class FundingRound(year: Int, amount: Long)
