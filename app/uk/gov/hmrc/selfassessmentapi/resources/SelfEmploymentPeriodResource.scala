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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Request}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentPeriodConnector
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.audit.PeriodicUpdate
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.{SelfEmploymentPeriod, SelfEmploymentPeriodUpdate}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse
import uk.gov.hmrc.selfassessmentapi.services.AuditService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentPeriodResource extends BaseResource {
  private lazy val FeatureSwitch = FeatureSwitchAction(SourceType.SelfEmployments, "periods")
  private val connector = SelfEmploymentPeriodConnector

  def createPeriod(nino: Nino, sourceId: SourceId): Action[JsValue] = FeatureSwitch.async(parse.json) {
    implicit request =>
      withAuth(nino) { implicit context =>
        validate[SelfEmploymentPeriod, (PeriodId, SelfEmploymentPeriodResponse)](request.body) { period =>
          connector
            .create(nino, sourceId, period)
            .map((period.createPeriodId, _))
        } match {
          case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
          case Right(result) =>
            result.map {
              case (periodId, response) =>
                response.filter {
                  case 200 =>
                    auditPeriodicCreate(nino, sourceId, response, periodId)
                    Created.withHeaders(LOCATION -> response.createLocationHeader(nino, sourceId, periodId))
                  case 400 if response.isInvalidBusinessId => NotFound
                  case 400 if response.isInvalidPeriod => Forbidden(Json.toJson(Errors.businessError(Errors.InvalidPeriod)))
                  case 400 if response.isInvalidNino => BadRequest(Json.toJson(Errors.NinoInvalid))
                  case 400 if response.isInvalidPayload  => BadRequest(Json.toJson(Errors.InvalidRequest))
                  case 404 => NotFound
                  case _ => unhandledResponse(response.status, logger)
                }
            }
        }
      }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def updatePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[JsValue] = FeatureSwitch.async(parse.json) {
    implicit request =>
      withAuth(nino) { implicit context =>
        validate[SelfEmploymentPeriodUpdate, SelfEmploymentPeriodResponse](request.body) { period =>
          connector.update(nino, id, periodId, period)
        } match {
          case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
          case Right(result) =>
            result.map { response =>
              response.filter {
                case 204 => NoContent
                case 400 => BadRequest(Error.from(response.json))
                case 404 => NotFound
                case _ => unhandledResponse(response.status, logger)
              }
            }
        }
      }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrievePeriod(nino: Nino, id: SourceId, periodId: PeriodId): Action[Unit] = FeatureSwitch.async(parse.empty) {
    implicit request =>
      withAuth(nino) { implicit context =>
        connector.get(nino, id, periodId).map { response =>
          response.filter {
            case 200 => response.period.map(x => Ok(Json.toJson(x))).getOrElse(NotFound)
            case 400 => BadRequest(Error.from(response.json))
            case 404 => NotFound
            case _ => unhandledResponse(response.status, logger)
          }
        }
      }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrievePeriods(nino: Nino, id: SourceId): Action[Unit] = FeatureSwitch.async(parse.empty) { implicit request =>
    withAuth(nino) { implicit context =>
      connector.getAll(nino, id).map { response =>
        response.filter {
          case 200 => Ok(Json.toJson(response.allPeriods))
          case 400 => BadRequest(Error.from(response.json))
          case 404 => NotFound
          case _ => unhandledResponse(response.status, logger)
        }
      }
    }
  }

  private def auditPeriodicCreate(nino: Nino,
                                  id: SourceId,
                                  response: SelfEmploymentPeriodResponse,
                                  periodId: PeriodId)(implicit hc: HeaderCarrier, request: Request[JsValue]): Unit = {
    AuditService.audit(payload = PeriodicUpdate(nino, id, periodId, response.transactionReference, request.body),
                       "self-employment-periodic-create")
  }
}
