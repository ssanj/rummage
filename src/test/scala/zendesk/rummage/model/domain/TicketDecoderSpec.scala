package zendesk.rummage.model.domain

import org.scalatest.flatspec.AnyFlatSpec
import io.circe.parser.decode

import DomainHelper._
import io.circe.DecodingFailure
import io.circe.CursorOp

final class TicketDecoderSpec extends AnyFlatSpec {

  private val completeTicketJson =
    """
    |{
    |  "_id": "c73a0be5-e967-4948-b0a4-eff98d1a43ad",
    |  "created_at": "2016-06-12T09:32:30-10:00",
    |  "type": "problem",
    |  "subject": "A Catastrophe in Maldives",
    |  "assignee_id": 34,
    |  "tags": [
    |    "Virginia",
    |    "Virgin Islands",
    |    "Maine",
    |    "West Virginia"
    |  ]
    |}
    """.stripMargin

  private val onlyIdJson =
    """
    | {
    |   "_id": "87db32c5-76a3-4069-954c-7d59c6c21de0"
    | }
    """.stripMargin

  private val invalidTicketJson =
    """
    | {
    |   "uid": "124564AA88998899898CE32423423",
    |   "name": "Brôôks Burke"
    | }
    """.stripMargin


  "A TicketDecoder" should "decode a complete ticket input" in {
    val decodeResult = decode[Ticket](completeTicketJson)
    val tags = List("Virginia", "Virgin Islands", "Maine", "West Virginia")
    val expectedTicket =
      createTicket(
        "c73a0be5-e967-4948-b0a4-eff98d1a43ad",
        "2016-06-12T09:32:30-10:00",
        "problem",
        "A Catastrophe in Maldives",
        34,
        tags
      )

    assertDecoder(decodeResult, expectedTicket)
  }

  it should "decode a ticket with only id" in {
    val decodeResult = decode[Ticket](onlyIdJson)
    val expectedTicket = createSimpleTicket("87db32c5-76a3-4069-954c-7d59c6c21de0")

    assertDecoder(decodeResult, expectedTicket)
  }

  it should "fail to decode an invalid ticket" in {
    val decodeResult = decode[Ticket](invalidTicketJson)
    assertDecoderError(decodeResult, {
      case e: DecodingFailure => {
        assert(e.getMessage.startsWith("Attempt to decode value on failed cursor"))
        assert(e.history == List(CursorOp.DownField("_id")))
      }
      case e2 => fail(s"Expected DecodingFailure but got: $e2")
    })
  }



}



