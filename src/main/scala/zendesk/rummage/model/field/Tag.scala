package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class Tag(value: String) extends AnyVal

object Tag {
  implicit val tagDecoder: Decoder[Tag] = Decoder[String].map(Tag.apply)
  implicit val tagEncoder: Encoder[Tag] = Encoder[String].contramap(_.value)

  implicit val tagToFieldValue: ToFieldValue[Tag] = ToFieldValue[String].contramap(_.value)
}
