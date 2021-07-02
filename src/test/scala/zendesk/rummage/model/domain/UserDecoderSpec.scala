package zendesk.rummage.model.domain

import org.scalatest.flatspec.AnyFlatSpec
import io.circe.parser.decode

import DomainHelper._
import io.circe.DecodingFailure
import io.circe.CursorOp

final class UserDecoderSpec extends AnyFlatSpec {

  private val completeUserJson =
    """
    | {
    |   "_id": 35,
    |   "name": "Brôôks Burke",
    |   "created_at": "2016-03-18T07:23:10-11:00",
    |   "verified": false
    | }
    """.stripMargin

  private val onlyIdJson =
    """
    | {
    |   "_id": 100
    | }
    """.stripMargin

  private val invalidUserJson =
    """
    | {
    |   "uid": "124564AA88998899898CE32423423",
    |   "name": "Brôôks Burke"
    | }
    """.stripMargin


  "A UserDecoder" should "decode a complete user input" in {
    val decodeResult = decode[User](completeUserJson)
    val expectedUser = createUser(35, "Brôôks Burke", "2016-03-18T07:23:10-11:00", false)

    assertDecoder(decodeResult, expectedUser)
  }

  it should "decode a user with only id" in {
    val decodeResult = decode[User](onlyIdJson)
    val expectedUser = createSimpleUser(100)

    assertDecoder(decodeResult, expectedUser)
  }

  it should "fail to decode an invalid user" in {
    val decodeResult = decode[User](invalidUserJson)
    assertDecoderError(decodeResult, {
      case e: DecodingFailure => {
        assert(e.getMessage.startsWith("Attempt to decode value on failed cursor"))
        assert(e.history == List(CursorOp.DownField("_id")))
      }
      case e2 => fail(s"Expected DecodingFailure but got: $e2")
    })
  }
}



