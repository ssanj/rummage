package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class UserId(value: Long) extends AnyVal

object UserId {
  implicit val userIdDecoder: Decoder[UserId] = Decoder[Long].map(UserId.apply)
  implicit val userIdEncoder: Encoder[UserId] = Encoder[Long].contramap(_.value)

  implicit val userIdToFieldValue: ToFieldValue[UserId] = ToFieldValue[Long].contramap(_.value)
}
