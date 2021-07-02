package zendesk.rummage.cli.algebra

import zendesk.rummage.model.search.SearchResult
import zendesk.rummage.model.TableType

trait SearchResultsPrinter {
  def print(searchResult: SearchResult): Vector[String]
}

object SearchResultsPrinter {
  
  final class SimpleSearchResultsPrinter(
    userSearchResultsPrinter: UserSearchResultsPrinter,
    ticketSearchResultsPrinter: TicketSearchResultsPrinter
  ) extends SearchResultsPrinter {

    override def print(searchResult: SearchResult): Vector[String] = searchResult match {
      case SearchResult.UserSearchResult(users) =>
        userSearchResultsPrinter.printEnrichedUsers(users)

      case SearchResult.TicketSearchResult(tickets) =>
        ticketSearchResultsPrinter.printEnrichedTickets(tickets)

      case SearchResult.InvalidFieldNameError(tableType, jsonFieldName) =>
        Vector(s"invalid field: `${jsonFieldName.value}` for table ${TableType.simpleString(tableType)}")
    }
  }
}
