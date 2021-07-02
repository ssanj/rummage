package zendesk.rummage.model.field

import io.circe.Decoder
import io.circe.Encoder
import zendesk.rummage.model.domain.typeclass.ToFieldValue

final case class Subject(value: String) extends AnyVal

object Subject {
  implicit val subjectDecoder: Decoder[Subject] = Decoder[String].map(Subject.apply)
  implicit val subjectEncoder: Encoder[Subject] = Encoder[String].contramap(_.value)

  implicit val subjectToFieldValue: ToFieldValue[Subject] = ToFieldValue[String].contramap(_.value)
}
