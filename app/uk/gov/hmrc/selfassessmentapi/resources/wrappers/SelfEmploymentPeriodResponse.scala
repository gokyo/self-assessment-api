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

package uk.gov.hmrc.selfassessmentapi.resources.wrappers

import play.api.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HttpResponse
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.des.{DesError, DesErrorCode}
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentPeriod

case class SelfEmploymentPeriodResponse(underlying: HttpResponse) extends ResponseFilter {


  private val logger: Logger = Logger(classOf[SelfEmploymentPeriodResponse])
  val status: Int = underlying.status
  def json: JsValue = underlying.json

  def createLocationHeader(nino: Nino, id: SourceId, periodId: PeriodId): String = {
    s"/self-assessment/ni/$nino/${SourceType.SelfEmployments.toString}/$id/periods/$periodId"
  }

  def period: Option[SelfEmploymentPeriod] = {
    json.asOpt[des.SelfEmploymentPeriod] match {
      case Some(desPeriod) =>
        Some(SelfEmploymentPeriod.from(desPeriod).copy(id = None))
      case None =>
        logger.error("The response from DES does not match the expected self-employment period format.")
        None
    }
  }

  def allPeriods: Seq[PeriodSummary] = {
    json.asOpt[Seq[des.SelfEmploymentPeriod]] match {
      case Some(desPeriods) =>
        val from = SelfEmploymentPeriod.from _
        val setId = (p: SelfEmploymentPeriod) => p.copy(id = Some(p.createPeriodId))
        val fromDES = from andThen setId andThen (_.asSummary)
        desPeriods.map(fromDES).sorted
      case None =>
        logger.error("The response from DES does not match the expected self-employment period format.")
        Seq.empty
    }
  }

  def transactionReference: Option[String] = {
    (json \ "transactionReference").asOpt[String] match {
      case x @ Some(_) => x
      case None => {
        logger.error("The 'transactionReference' field was not found in the response from DES")
        None
      }
    }
  }

  def isInvalidNino: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_NINO)

  def isInvalidPayload: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_PAYLOAD)

  def isInvalidPeriod: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_PERIOD)

  def isInvalidBusinessId: Boolean =
    json.asOpt[DesError].exists(_.code == DesErrorCode.INVALID_BUSINESSID)
}

