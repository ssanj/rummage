package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class Name(value: String) extends AnyVal

object Name {
  implicit val nameDecoder: Decoder[Name] = Decoder[String].map(Name.apply)
  implicit val nameEncoder: Encoder[Name] = Encoder[String].contramap(_.value)

  implicit val nameToFieldValue: ToFieldValue[Name] = ToFieldValue[String].contramap(_.value)
}
