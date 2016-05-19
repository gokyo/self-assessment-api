/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.domain

import play.api.libs.functional.syntax._
import play.api.libs.json.Reads._
import play.api.libs.json._

case class SelfEmploymentAllowances(annualInvestmentAllowance: Option[BigDecimal] = None,
                                    capitalAllowanceMainPool: Option[BigDecimal] = None,
                                    capitalAllowanceSpecialRatePool: Option[BigDecimal] = None,
                                    restrictedCapitalAllowance: Option[BigDecimal] = None,
                                    businessPremisesRenovationAllowance: Option[BigDecimal] = None,
                                    enhancedCapitalAllowance: Option[BigDecimal] = None,
                                    allowancesOnSales: Option[BigDecimal] = None)

object SelfEmploymentAllowances {
  implicit val writes = Json.writes[SelfEmploymentAllowances]

  implicit val reads: Reads[SelfEmploymentAllowances] = (
      (__ \ "annualInvestmentAllowance").readNullable[BigDecimal](amountValidator("annualInvestmentAllowance")) and
      (__ \ "capitalAllowanceMainPool").readNullable[BigDecimal](amountValidator("capitalAllowanceMainPool")) and
      (__ \ "capitalAllowanceSpecialRatePool").readNullable[BigDecimal](amountValidator("capitalAllowanceSpecialRatePool")) and
      (__ \ "restrictedCapitalAllowance").readNullable[BigDecimal](amountValidator("restrictedCapitalAllowance")) and
      (__ \ "businessPremisesRenovationAllowance").readNullable[BigDecimal](amountValidator("businessPremisesRenovationAllowance")) and
      (__ \ "enhancedCapitalAllowance").readNullable[BigDecimal](amountValidator("enhancedCapitalAllowance")) and
      (__ \ "allowancesOnSales").readNullable[BigDecimal](amountValidator("allowancesOnSales"))
    ) (SelfEmploymentAllowances.apply _)
}
