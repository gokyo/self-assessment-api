package uk.gov.hmrc.selfassessmentapi

import reactivemongo.bson.BSONObjectID
import uk.gov.hmrc.support.BaseFunctionalSpec

class AcceptHeaderSpec extends BaseFunctionalSpec {
  val selfEmploymentId = BSONObjectID.generate.stringify

  "if the valid content type header is sent in the request, they" should {
    "receive 204" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/nino/$nino/self-employments").withAcceptHeader()
        .thenAssertThat().statusIs(204)
    }
  }

  "if the valid content type header is missing in the request, they" should {
    "receive 406" in {
      given()
        .userIsAuthorisedForTheResource(nino)
        .when()
        .get(s"/nino/$nino/self-employments").withoutAcceptHeader()
        .thenAssertThat()
        .statusIs(406)
        .bodyIsError("ACCEPT_HEADER_INVALID")
    }
  }

}
