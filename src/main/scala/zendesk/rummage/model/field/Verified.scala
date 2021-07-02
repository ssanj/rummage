package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class Verified(value: Boolean) extends AnyVal

object Verified {
  implicit val verifiedDecoder: Decoder[Verified] = Decoder[Boolean].map(Verified.apply)
  implicit val verifiedEncoder: Encoder[Verified] = Encoder[Boolean].contramap(_.value)

  implicit val verifiedToFieldValue: ToFieldValue[Verified] = ToFieldValue[Boolean].contramap(_.value)
}
