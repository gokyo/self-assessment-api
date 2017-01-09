/*
 * Copyright 2017 HM Revenue & Customs
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

import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue}
import uk.gov.hmrc.selfassessmentapi.resources.models.TaxYear
import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{FHLPropertiesAnnualSummary, OtherPropertiesAnnualSummary}

/**
  * Provides a suite of JSON formats for objects used throughout the codebase.
  */
object JsonFormatters {

  sealed abstract class MapEnumFormat[K <: Enumeration#Value, V: Format] extends Format[Map[K, V]] {
    override def writes(o: Map[K, V]): JsValue = {
      val mapped = o.map {
        case (k, v) => k.toString -> v
      }
      play.api.libs.json.Writes.mapWrites[V].writes(mapped)
    }

    override def reads(json: JsValue): JsResult[Map[K, V]]
  }

  object SelfEmploymentFormatters {

    import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.SelfEmploymentAnnualSummary
    import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.BalancingChargeType.BalancingChargeType
    import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.ExpenseType.ExpenseType
    import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.IncomeType.IncomeType
    import uk.gov.hmrc.selfassessmentapi.resources.models.selfemployment.{BalancingChargeType, ExpenseType, IncomeType}

    implicit def incomeTypeFormat[V: Format]: MapEnumFormat[IncomeType, V] = new MapEnumFormat[IncomeType, V] {
      override def reads(json: JsValue): JsResult[Map[IncomeType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => IncomeType.withName(k) -> v
          })
        }
      }
    }

    implicit def expenseTypeFormat[V: Format]: MapEnumFormat[ExpenseType, V] = new MapEnumFormat[ExpenseType, V] {
      override def reads(json: JsValue): JsResult[Map[ExpenseType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => ExpenseType.withName(k) -> v
          })
        }
      }
    }

    implicit def balancingChargeTypeFormat[V: Format]: MapEnumFormat[BalancingChargeType, V] = new MapEnumFormat[BalancingChargeType, V] {
      override def reads(json: JsValue): JsResult[Map[BalancingChargeType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => BalancingChargeType.withName(k) -> v
          })
        }
      }
    }

    implicit val annualSummaryMapFormat: Format[Map[TaxYear, SelfEmploymentAnnualSummary]] = new Format[Map[TaxYear, SelfEmploymentAnnualSummary]] {
      override def writes(o: Map[TaxYear, SelfEmploymentAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[SelfEmploymentAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, SelfEmploymentAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[SelfEmploymentAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

  object PropertiesFormatters {

    import uk.gov.hmrc.selfassessmentapi.resources.models.properties.ExpenseType.ExpenseType
    import uk.gov.hmrc.selfassessmentapi.resources.models.properties.IncomeType.IncomeType
    import uk.gov.hmrc.selfassessmentapi.resources.models.properties.{ ExpenseType, IncomeType}

    implicit def incomeTypeFormat[V: Format]: MapEnumFormat[IncomeType, V] = new MapEnumFormat[IncomeType, V] {
      override def reads(json: JsValue): JsResult[Map[IncomeType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => IncomeType.withName(k) -> v
          })
        }
      }
    }

    implicit def expenseTypeFormat[V: Format]: MapEnumFormat[ExpenseType, V] = new MapEnumFormat[ExpenseType, V] {
      override def reads(json: JsValue): JsResult[Map[ExpenseType, V]] = {
        play.api.libs.json.Reads.mapReads[V].reads(json).flatMap { result =>
          JsSuccess(result.map {
            case (k, v) => ExpenseType.withName(k) -> v
          })
        }
      }
    }

    implicit val annualSummaryOtherMapFormat: Format[Map[TaxYear, OtherPropertiesAnnualSummary]] = new Format[Map[TaxYear, OtherPropertiesAnnualSummary]] {
      override def writes(o: Map[TaxYear, OtherPropertiesAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[OtherPropertiesAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, OtherPropertiesAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[OtherPropertiesAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }

    implicit val annualSummaryFHLMapFormat: Format[Map[TaxYear, FHLPropertiesAnnualSummary]] = new Format[Map[TaxYear, FHLPropertiesAnnualSummary]] {
      override def writes(o: Map[TaxYear, FHLPropertiesAnnualSummary]): JsValue = {
        play.api.libs.json.Writes.mapWrites[FHLPropertiesAnnualSummary].writes(o.map {
          case (k, v) => k.toString -> v
        })
      }

      override def reads(json: JsValue): JsResult[Map[TaxYear, FHLPropertiesAnnualSummary]] = {
        play.api.libs.json.Reads.mapReads[FHLPropertiesAnnualSummary].reads(json).map(_.map {
          case (k, v) => TaxYear(k) -> v
        })
      }
    }
  }

}
