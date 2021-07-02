package zendesk.rummage.cli.algebra

import zendesk.rummage.model.domain.DataMapper
import zendesk.rummage.model.domain.User
import zendesk.rummage.model.field.TicketId
import zendesk.rummage.model.JsonFieldName
import zendesk.rummage.model.search.EnrichedTicket
import zendesk.rummage.model.domain.Ticket


trait TicketSearchResultsPrinter {
  def printEnrichedTickets(tickets: Vector[EnrichedTicket]): Vector[String]
}

object TicketSearchResultsPrinter {

  import cats.data.NonEmptyVector
  final class LiveTicketSearchResultsPrinter extends TicketSearchResultsPrinter {

    private val ticketDataMapper = DataMapper[TicketId, Ticket]
    private val ticketJsonFieldNameMapping = ticketDataMapper.getJsonFieldNameAndValues _
    private val maxFieldLength: Int = lengthOfLongestFieldName(ticketDataMapper.jsonFieldNames)

    override def printEnrichedTickets(tickets: Vector[EnrichedTicket]): Vector[String] = {
      if (tickets.isEmpty) noResultsFound
      else {
        tickets.zipWithIndex.map {
          case (et, index) => printEnrichedTicket(index + 1, et)
        }
      }
    }

    private def printEnrichedTicket(index: Int, enrichedTicket: EnrichedTicket): String = {
      val ticketString = printTicket(index, enrichedTicket.ticket)
      val assigneeStrings: Vector[String] =
        enrichedTicket.assignee.fold({
          Vector(
            "",
            "= No Assignee ="
          )}) { assignee =>
          Vector(
            "",
            "= Assignee =",
            printTicketAssignee(assignee),
            ""
          )
        }

       (ticketString +: assigneeStrings).mkString("\n")
    }

    private def printTicket(index: Int, ticket: Ticket): String = {
      val ticketStrings =
        ticketJsonFieldNameMapping(ticket).map {
          case (jsonFieldName, fieldValue) =>
            val padding = getPadding(jsonFieldName.value)
            s"${jsonFieldName.value}${padding} -> ${fieldValue.value}"
        }

      ticketStrings.toVector.mkString(
        s"\n== Ticket ${index.toString} ==\n",
        "\n",
        ""
      )
    }

    private def printTicketAssignee(user: User): String = {
      val userId = user.id.value.toString
      val userName = user.name.map(_.value).getOrElse("< did not have user name >")
      s"${userId}:${userName}"
    }

    private def getPadding(fieldName: String): String =  {
        val fillLength = Math.max(maxFieldLength - fieldName.length, 0)
        Vector.fill(fillLength)(" ").mkString
      }
    }

    private def noResultsFound: Vector[String] =
      Vector(
        "",
        "No results found :(",
        ""
      )

    private def lengthOfLongestFieldName(fields: NonEmptyVector[JsonFieldName]): Int = {
      val lengthOfAllFieldNames = fields.map(j => j.value.length)
      getMaxLength(lengthOfAllFieldNames)
    }
}
