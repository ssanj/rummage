package zendesk.rummage.model
package domain

import io.circe.Decoder
import io.circe.Encoder
import cats.data.NonEmptyVector

import zendesk.rummage.model.field._
import zendesk.rummage.model.domain.typeclass.ToFieldValue

/**
 * Domain object that maps to tickets.json.
 * All fields but the primary key (id) are optional.
 *
 * See docs/data_quality.md for details
 * @note If any field names in Tickets domain object or tickets.json input change, you need to update the
 * encoder/decoder/dataMapper instances defined in this file.
 */
final case class Ticket(
  id: TicketId,                    //_id
  createdAt: Option[Timestamp],    //created_at
  ticketType: Option[TicketType],  //type
  subject: Option[Subject],        //subject
  assigneeId: Option[UserId],      //assignee_id
  tags: Option[Seq[Tag]]           //tags
)

object Ticket {

  implicit val decoderTicket: Decoder[Ticket] =
    Decoder.forProduct6(
      "_id",
      "created_at",
      "type",
      "subject",
      "assignee_id",
      "tags"
    )(Ticket.apply)

  implicit val encoderTicket: Encoder[Ticket] =
    Encoder.forProduct6(
      "_id",
      "created_at",
      "type",
      "subject",
      "assignee_id",
      "tags"
    )(t => (t.id, t.createdAt, t.ticketType, t.subject, t.assigneeId, t.tags))

  private val fieldMappings: NonEmptyVector[DomainFieldMapping[Ticket]] = {
      NonEmptyVector.of[DomainFieldMapping[Ticket]](
        DomainFieldMapping(
          JsonFieldName("_id"), FieldName("id"), value => ToFieldValue[TicketId].toFieldValue(value.id)),
         DomainFieldMapping(
          JsonFieldName("created_at"), FieldName("createdAt"), value => ToFieldValue[Option[Timestamp]].toFieldValue(value.createdAt)),
        DomainFieldMapping(
          JsonFieldName("type"), FieldName("ticketType"), value => ToFieldValue[Option[TicketType]].toFieldValue(value.ticketType)),
        DomainFieldMapping(
          JsonFieldName("subject"), FieldName("subject"), value => ToFieldValue[Option[Subject]].toFieldValue(value.subject)),
        DomainFieldMapping(
          JsonFieldName("assignee_id"), FieldName("assigneeId"), value => ToFieldValue[Option[UserId]].toFieldValue(value.assigneeId)),
        DomainFieldMapping(
          JsonFieldName("tags"), FieldName("tags"), value => ToFieldValue[Option[Seq[Tag]]].toFieldValue(value.tags))
      )
    }

  implicit val dataMapperTicket: DataMapper[TicketId, Ticket] = DataMapper.from[TicketId, Ticket](fieldMappings, _.id)

  /**
   * Foreign key [[zendesk.rummage.model.FieldName]] for assigneeId
   */
  val assigneeIdFK: FieldName = FieldName("assigneeId")
}

