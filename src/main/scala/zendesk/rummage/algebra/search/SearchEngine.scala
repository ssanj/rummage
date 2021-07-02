package zendesk.rummage.algebra.search

import zendesk.rummage.model.SearchQuery
import zendesk.rummage.model.search.SearchResult
import zendesk.rummage.model.search.EnrichedUser
import zendesk.rummage.model.search.EnrichedTicket

/**
 * Models a "search engine", which is basically a function from some query to some results:
 * {{{
 *  SearchQuery => SearchResult
 * }}}
 */
trait SearchEngine {

  /**
   * Given a [[zendesk.rummage.model.SearchQuery]] returns a [[zendesk.rummage.model.search.SearchResult]]
   * of any matching domain objects.
   * @param query The search query entered by the user
   * @return Matching search results
   */
  def performSearch(query: SearchQuery): SearchResult
}

object SearchEngine {

  import zendesk.rummage.model.FieldName
  import zendesk.rummage.model.domain.User
  import zendesk.rummage.model.domain.Ticket
  import zendesk.rummage.model.field.UserId
  import zendesk.rummage.model.field.TicketId
  import zendesk.rummage.model.domain.DataMapper
  import zendesk.rummage.model.TableType
  import zendesk.rummage.algebra.search.table.UserTable
  import zendesk.rummage.algebra.search.table.TicketTable

  final class LiveSearchEngine(userTable: UserTable, ticketTable: TicketTable) extends SearchEngine {

    private val userJsonFieldMapping   = DataMapper[UserId, User].domainFieldForJsonField _
    private val ticketJsonFieldMapping = DataMapper[TicketId, Ticket].domainFieldForJsonField _

    override def performSearch(query: SearchQuery): SearchResult = query match {

      case SearchQuery(TableType.UserTableType, jsonField, fieldValue) =>
        val domainFieldNameOp: Option[FieldName] = userJsonFieldMapping(jsonField)

        domainFieldNameOp match {
          case Some(domainFieldName) =>
            val matchingUsers = userTable.findBy(domainFieldName, fieldValue)
            val usersAndTiickets = matchingUsers.map(u => EnrichedUser(u, ticketTable.findByAssigneeId(u.id)))
            SearchResult.UserSearchResult(usersAndTiickets)

          case None => SearchResult.InvalidFieldNameError(TableType.UserTableType, jsonField)
        }

      case SearchQuery(TableType.TicketTableType, jsonField, fieldValue) =>
        val domainFieldNameOp: Option[FieldName] = ticketJsonFieldMapping(jsonField)

        domainFieldNameOp match {
          case Some(domainFieldName) =>
            val matchingTickets = ticketTable.findBy(domainFieldName, fieldValue)
            val enrichedTickets = matchingTickets.map(t => EnrichedTicket(t, t.assigneeId.flatMap(userTable.findByPrimaryKey)))
            SearchResult.TicketSearchResult(enrichedTickets)

          case None => SearchResult.InvalidFieldNameError(TableType.TicketTableType, jsonField)
        }

    }


  }
}
