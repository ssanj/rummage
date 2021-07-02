package zendesk.rummage.algebra.search.table

import zendesk.rummage.model.field.TicketId
import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.domain.typeclass.ToFieldValue

trait TicketTable extends Table[TicketId, Ticket] {
  def assigneeIdFK: FieldName
  def findByAssigneeId(userId: UserId): Vector[Ticket]
}

object TicketTable {

  final class LiveTicketTable(tableData: TableData[TicketId, Ticket], fieldsIndex: FieldsIndex[TicketId]) extends TicketTable {

    private val genericTable: Table.GenericTable[TicketId, Ticket] = new Table.GenericTable(tableData, fieldsIndex)

    override def findBy(fieldName: FieldName, fieldValue: FieldValue): Vector[Ticket] = {
      genericTable.findBy(fieldName, fieldValue)
    }

    override def assigneeIdFK: FieldName = Ticket.assigneeIdFK

    override def findByAssigneeId(assigneeId: UserId): Vector[Ticket] = {
      val assigneeIdFieldValue: FieldValue = ToFieldValue[UserId].toFieldValue(assigneeId)

      val optFoundTickets =
        for {
          optAssigneeIdValues <- fieldsIndex.get(assigneeIdFK)
          optAssigneeIds      <- optAssigneeIdValues.get(assigneeIdFieldValue)
          result = optAssigneeIds.map(tableData.get).flatten
        } yield result

      optFoundTickets.fold(Vector.empty[Ticket])(identity)
    }

    override def findByPrimaryKey(ticketId: TicketId): Option[Ticket] = genericTable.findByPrimaryKey(ticketId)
  }

}
