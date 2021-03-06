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

package uk.gov.hmrc.selfassessmentapi.models

object SourceType extends Enumeration {
  type SourceType = Value

  val SelfEmployments = Value("self-employments")
  val Properties = Value("uk-properties")
  val Banks = Value("savings-accounts")
  val Dividends = Value("dividends")
  val Calculation = Value("calculations")

  def sourceTypeToDocumentationName(sourceType: SourceType): String = sourceType match {
    case SelfEmployments => "SelfEmployments"
    case Properties => "Properties"
    case Dividends => "Dividends"
    case Banks => "SavingsAccounts"
    case Calculation => "TaxCalculation"
  }
}
