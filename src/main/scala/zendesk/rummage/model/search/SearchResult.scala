package zendesk.rummage.model
package search


/**
 * The type of result return for a user query.
 */
sealed trait SearchResult
object SearchResult {

  /**
   * User results.
   */
  final case class UserSearchResult(users: Vector[EnrichedUser]) extends SearchResult

  /**
   * Ticket results.
   */
  final case class TicketSearchResult(tickets: Vector[EnrichedTicket]) extends SearchResult

  /**
   * The field name supplied was invalid
   */
  final case class InvalidFieldNameError(tableType: TableType, jsonFieldName: JsonFieldName) extends SearchResult
}
