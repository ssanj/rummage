package zendesk.rummage.algebra.search

import org.scalatest.flatspec.AnyFlatSpec
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.domain.DomainHelper._

import zendesk.rummage.model.SearchQuery
import zendesk.rummage.model.TableType
import zendesk.rummage.model.search.SearchResult
import zendesk.rummage.model.JsonFieldName

final class LiveSearchEngineIntSpec extends AnyFlatSpec {

  private val user1 = createUser(1, "Francisca Rasmussen", "2016-04-15T05:19:46-10:00", true)
  private val user2 = createUser(2, "Cross Barlow", "2016-06-23T10:31:39-10:00", true)
  private val user3 = createUser(3, "Ingrid Wagner", "2016-07-28T05:29:25-10:00", false)

  private val ticket1 =
    createTicket(
      "7382ad0e-dea7-4c8d-b38f-cbbf016f2598",
      "2016-03-31T03:16:52-11:00",
      "task",
      "A Problem in American Samoa",
      64L,
      List("Missouri", "Alabama", "Virginia", "Virgin Islands")
    )

  private val userData = Vector(user1, user2, user3)
  private val ticketData = Vector(ticket1)

  private val searchEngine = new SearchEngineBuilder.LiveSearchEngineBuilder(userData, ticketData).build()

  "A LiveSearchEngine" should "find a matching query on a User" in {
    val query = SearchQuery(TableType.UserTableType, JsonFieldName("name"), FieldValue("Ingrid Wagner"))

    val searchResult = searchEngine.performSearch(query)

    searchResult match {
      case SearchResult.UserSearchResult(matchedUsers) =>
        assert(matchedUsers.length == 1)
        assert(matchedUsers(0).user == user3)

      case SearchResult.TicketSearchResult(tickets) => fail(s"expected a User match but got a Ticket match: ${tickets.mkString(",")}")

      case e: SearchResult.InvalidFieldNameError => fail(s"expected a match but got an InvalidFieldNameError: ${e.toString}")
    }
  }

  it should "not find any matches when there are none" in {
    val query = SearchQuery(TableType.UserTableType, JsonFieldName("_id"), FieldValue("100"))

    val searchResult = searchEngine.performSearch(query)

    searchResult match {
      case SearchResult.UserSearchResult(matchedUsers) => assert(matchedUsers.length == 0)
      case SearchResult.TicketSearchResult(tickets) => fail(s"expected a User match but got a Ticket match: ${tickets.mkString(",")}")
      case e: SearchResult.InvalidFieldNameError => fail(s"expected no matches but got an InvalidFieldNameError: ${e.toString}")
    }
  }

  it should "not find any matches when the json field name is not found" in {
    val query = SearchQuery(TableType.UserTableType, JsonFieldName("XYZ"), FieldValue("100"))

    val searchResult = searchEngine.performSearch(query)

    searchResult match {
      case SearchResult.InvalidFieldNameError(TableType.UserTableType, JsonFieldName("XYZ")) => assert(true)
      case SearchResult.InvalidFieldNameError(tableType, jsonFieldName) =>
        fail(s"expected tableType:UserTableType and jsonField:XYZ got tableType:${tableType.toString} and jsonField:${jsonFieldName.value}")
      case SearchResult.UserSearchResult(matchedUsers) => fail(s"expected no matches but got a User match: ${matchedUsers.map(_.toString).mkString(",")}")
      case SearchResult.TicketSearchResult(tickets) => fail(s"expected no matches but got a Ticket match: ${tickets.mkString(",")}")
    }
  }
}
