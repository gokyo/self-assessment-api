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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.selfassessmentapi.models.banks.Bank
import uk.gov.hmrc.selfassessmentapi.models.{Errors, SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.services.BanksService

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

object BanksResource extends BaseResource {
  private lazy val FeatureSwitch = FeatureSwitchAction(SourceType.Banks)
  private val service = BanksService

  def create(nino: Nino): Action[JsValue] = FeatureSwitch.async(parse.json) { implicit request =>
    withAuth(nino) { _ =>
      validate[Bank, Option[SourceId]](request.body) { bank =>
        service.create(nino, bank)
      } match {
        case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
        case Right(idOption) => idOption.map {
          case Some(id) => Created.withHeaders(LOCATION -> s"/self-assessment/ni/$nino/savings-accounts/$id")
          case None => InternalServerError
        }
      }
    }
  }

  def update(nino: Nino, id: SourceId): Action[JsValue] = FeatureSwitch.async(parse.json) { implicit request =>
    withAuth(nino) { context =>
      validate[Bank, Boolean](request.body) { bank =>
        service.update(nino, bank, id)
      } match {
        case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
        case Right(result) => result.map {
          case true => NoContent
          case false if context.isFOA => BadRequest(Json.toJson(Errors.InvalidRequest))
          case false => NotFound
        }
      }
    }
  }

  def retrieve(nino: Nino, id: SourceId): Action[AnyContent] = FeatureSwitch.async { implicit headers =>
    withAuth(nino) { context =>
      service.retrieve(nino, id) map {
        case Some(bank) => Ok(Json.toJson(bank))
        case None if context.isFOA => BadRequest(Json.toJson(Errors.InvalidRequest))
        case None => NotFound
      }
    }
  }

  def retrieveAll(nino: Nino): Action[AnyContent] = FeatureSwitch.async { implicit headers =>
    withAuth(nino) { _ =>
      service.retrieveAll(nino) map { seq =>
        Ok(Json.toJson(seq))
      }
    }
  }
}
