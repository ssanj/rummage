package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class Timestamp(value: String) extends AnyVal

object Timestamp {
  implicit val timestampDecoder: Decoder[Timestamp] = Decoder[String].map(Timestamp.apply)
  implicit val timestampEncoder: Encoder[Timestamp] = Encoder[String].contramap(_.value)

  implicit val timestampToFieldValue: ToFieldValue[Timestamp] = ToFieldValue[String].contramap(_.value)
}
