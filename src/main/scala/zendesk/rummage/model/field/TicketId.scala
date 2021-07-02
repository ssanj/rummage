package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class TicketId(value: String) extends AnyVal

object TicketId {
  implicit val ticketIdDecoder: Decoder[TicketId] = Decoder[String].map(TicketId.apply)
  implicit val ticketIdEncoder: Encoder[TicketId] = Encoder[String].contramap(_.value)

  implicit val ticketIdToFieldValue: ToFieldValue[TicketId] = ToFieldValue[String].contramap(_.value)
}
