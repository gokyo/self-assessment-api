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

package uk.gov.hmrc.selfassessmentapi.controllers

import org.scalatest.mock.MockitoSugar
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.play.auth.microservice.connectors.ConfidenceLevel
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.selfassessmentapi.UnitSpec

import scala.concurrent.Future

class CustomerResolverControllerSpec extends UnitSpec with MockitoSugar {

  val saUtr: SaUtr = generateSaUtr()
  val authConfidenceLevel: ConfidenceLevel = ConfidenceLevel.L500
  val headerCarrier: HeaderCarrier = HeaderCarrier()

  def testController(utr: Option[SaUtr]) = new BaseCustomerResolverController {
    override def selfAssessmentUrl(saUtr: SaUtr): String = "selfAssessmentUrl"
    override def saUtr(confidenceLevel: ConfidenceLevel)(implicit hc: HeaderCarrier): Future[Option[SaUtr]] =  Future.successful(utr)
    override val confidenceLevel: ConfidenceLevel = authConfidenceLevel
    override def hc(request: Request[Any]): HeaderCarrier = headerCarrier
    override val context: String = "/self-assessment"
  }


  "resolve" should {
    val request = FakeRequest("GET", s"/")
      .withHeaders(("Accept", "application/vnd.hmrc.1.0+json"))

    "return a successful response with a link to the self-assessment customer details endpoint" in {
      val result = testController(Some(saUtr)).resolve()(request)

      status(result) shouldEqual OK
      val response = contentAsJson(result)
      val links = response \ "_links"
      val saLink = links \ "self-assessment"

      (saLink \ "href").as[String] shouldEqual "selfAssessmentUrl"
    }

    "return unauthorised when no sa utr is found" in {
      val result = testController(None).resolve()(request)

      status(result) shouldEqual UNAUTHORIZED
    }
  }
}