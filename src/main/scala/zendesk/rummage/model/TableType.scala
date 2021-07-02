package zendesk.rummage.model

/**
 * Represents which table the user chose to search on.
 */
sealed trait TableType
object TableType {
  /**
   * User table.
   */
  case object UserTableType         extends TableType

  /**
   * Ticket table.
   */
  case object TicketTableType       extends TableType

  /**
   * Simple String representation.
   */
  def simpleString(tableType: TableType): String = tableType match {
    case UserTableType   => "users"
    case TicketTableType => "tickets"
  }
}
