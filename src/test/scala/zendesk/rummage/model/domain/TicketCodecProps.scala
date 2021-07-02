package zendesk.rummage.model.domain

import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop.propBoolean
import zendesk.rummage.model.Generators._
import io.circe.syntax._

object TicketCodecProps extends Properties("Ticket Codec") {

  //Given a Ticket, encoding and then decoding it should return the original Ticket
  property("roundtrip") =
    Prop.forAll { (ticket: Ticket) =>
      val ticketJson = ticket.asJson
      val decodeResult = ticketJson.as[Ticket]
      decodeResult match {
        case Right(decodedTicket) =>
          (ticket == decodedTicket) :| s"tickets did not match, expected: $$ticket\ngot: $decodedTicket\njson: $ticketJson"
        case Left(e)              =>
          false :| s"decoding error: $e\nticket: $ticket\njson: $ticketJson"
      }
    }
}
