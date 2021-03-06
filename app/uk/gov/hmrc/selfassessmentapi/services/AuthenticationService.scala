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

package uk.gov.hmrc.selfassessmentapi.services

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.config.MicroserviceAuthConnector
import uk.gov.hmrc.selfassessmentapi.contexts.AuthContext
import uk.gov.hmrc.selfassessmentapi.models.{Errors, MtdId}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AuthenticationService extends AuthorisedFunctions {
  override def authConnector: AuthConnector = MicroserviceAuthConnector

  private val logger = Logger(AuthenticationService.getClass)

  def authorise(mtdId: MtdId)(f: AuthContext => Future[Result])
               (implicit hc: HeaderCarrier, reqHeader: RequestHeader): Future[Result] =
    authoriseAsClient(mtdId)(f)

  private def authoriseAsClient(mtdId: MtdId)(f: AuthContext => Future[Result])
                               (implicit hc: HeaderCarrier, requestHeader: RequestHeader): Future[Result] = {
    logger.debug("Attempting to authorise user a a fully-authorised individual.")
    authorised(
      Enrolment("HMRC-MTD-IT")
        .withIdentifier("MTDITID", mtdId.mtdId)
        .withDelegatedAuthRule("mtd-it-auth")) {
      logger.debug("Client authorisation succeeded as fully-authorised individual.")
      f(AuthContext(isFOA = false))
    } recoverWith (authoriseAsFOA(f) orElse unhandledError)
  }

  private def authoriseAsFOA(f: AuthContext => Future[Result])
                            (implicit hc: HeaderCarrier, reqHeader: RequestHeader): PartialFunction[Throwable, Future[Result]] = {
    case _: InsufficientEnrolments =>
      authorised(AffinityGroup.Agent) { // Is the user an agent?
        authorised(Enrolment("HMRC-AS-AGENT")) { // If so, are they enrolled in Agent Services?
          if (reqHeader.method == "GET") {
            logger.debug("Client authorisation failed. Attempt to GET as a filing-only agent.")
            Future.successful(Forbidden(Json.toJson(Errors.AgentNotAuthorized)))
          } else {
            logger.debug("Client authorisation succeeded as filing-only agent.")
            f(AuthContext(isFOA = true))
          }
        } recoverWith (unsubscribedAgent orElse unhandledError) // Iff agent is not enrolled for the user.
      } recoverWith (unauthorisedClient orElse unhandledError) // Iff client affinityGroup is not Agent.
  }

  private def unsubscribedAgent: PartialFunction[Throwable, Future[Result]] = {
    case _: InsufficientEnrolments =>
      logger.debug(s"Authorisation failed as filing-only agent.")
      Future.successful(Forbidden(Json.toJson(Errors.AgentNotSubscribed)))
  }

  private def unauthorisedClient: PartialFunction[Throwable, Future[Result]] = {
    case _: UnsupportedAffinityGroup =>
      logger.debug(s"Authorisation failed as client.")
      Future.successful(Forbidden(Json.toJson(Errors.ClientNotSubscribed)))
  }

  private def unhandledError: PartialFunction[Throwable, Future[Result]] = {
    case e: AuthorisationException =>
      logger.error(s"Authorisation failed with unexpected exception. Bad token? Exception: [$e]")
      Future.successful(Forbidden(Json.toJson(Errors.BadToken)))
  }
}
