package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class TicketType(value: String) extends AnyVal

object TicketType {
  implicit val ticketTypeDecoder: Decoder[TicketType] = Decoder[String].map(TicketType.apply)
  implicit val ticketTypeEncoder: Encoder[TicketType] = Encoder[String].contramap(_.value)

  implicit val ticketTypeToFieldValue: ToFieldValue[TicketType] = ToFieldValue[String].contramap(_.value)
}
