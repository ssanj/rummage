package zendesk.rummage.model
package domain

import zendesk.rummage.model.FieldName
import zendesk.rummage.model.JsonFieldName

import zendesk.rummage.model.domain.typeclass.ToFieldValue

import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.field.Name
import zendesk.rummage.model.field.Timestamp
import zendesk.rummage.model.field.Verified

import io.circe.Decoder
import io.circe.Encoder
import cats.data.NonEmptyVector

/**
 * Domain object that maps to users.json.
 * All fields but the primary key (id) are optional.
 *
 * See docs/data_quality.md for details
 * @note If any field names in User domain object or user.json input change, you need to update the
 * encoder/decoder/dataMapper instances defined in this file.
 */
final case class User(
  id: UserId,                      //_id
  name: Option[Name],              //name
  createdAt: Option[Timestamp],    //created_at
  verified: Option[Verified]       //verified
)

object User {
  //create the encoder using the least magic possible.
  implicit val decodeUser: Decoder[User] =
    Decoder.forProduct4(
      "_id",
      "name",
      "created_at",
      "verified"
    )(User.apply)

  //decoder using the least magic possible.
  implicit val encoderUser: Encoder[User] =
    Encoder.forProduct4(
      "_id",
      "name",
      "created_at",
      "verified"
    )(u => (u.id, u.name, u.createdAt, u.verified))

  private val fieldMappings: NonEmptyVector[DomainFieldMapping[User]] = {
    NonEmptyVector.of[DomainFieldMapping[User]](
      DomainFieldMapping(
        JsonFieldName("_id"), FieldName("id"), value => ToFieldValue[UserId].toFieldValue(value.id)),
       DomainFieldMapping(
        JsonFieldName("name"), FieldName("name"), value => ToFieldValue[Option[Name]].toFieldValue(value.name)),
      DomainFieldMapping(
        JsonFieldName("created_at"), FieldName("createdAt"), value => ToFieldValue[Option[Timestamp]].toFieldValue(value.createdAt)),
      DomainFieldMapping(
        JsonFieldName("verified"), FieldName("verified"), value => ToFieldValue[Option[Verified]].toFieldValue(value.verified))
    )
  }

  implicit val dataMapperUser: DataMapper[UserId, User] = DataMapper.from[UserId, User](fieldMappings, _.id)
}


