package zendesk.rummage.cli.algebra

import zendesk.rummage.model.domain.DataMapper
import zendesk.rummage.model.domain.User
import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.JsonFieldName
import zendesk.rummage.model.search.EnrichedUser
import zendesk.rummage.model.domain.Ticket

trait UserSearchResultsPrinter {
  def printEnrichedUsers(users: Vector[EnrichedUser]): Vector[String]
}

object UserSearchResultsPrinter {

  import cats.data.NonEmptyVector

  final class LiveUserSearchResultsPrinter extends UserSearchResultsPrinter {

    private val userDataMapper = DataMapper[UserId, User]
    private val userJsonFieldNameMapping = userDataMapper.getJsonFieldNameAndValues _
    private val maxFieldLength: Int = lengthOfLongestFieldName(userDataMapper.jsonFieldNames)

    override def printEnrichedUsers(users: Vector[EnrichedUser]): Vector[String] = {
      if (users.isEmpty) noResultsFound
      else {
        users.zipWithIndex.map {
          case (eu, index) => printEnrichedUser(index + 1, eu)
        }
      }
    }

    private def printEnrichedUser(index: Int, enrichedUser: EnrichedUser): String = {
      val userString = printUser(index, enrichedUser.user)
      val ticketStrings: Vector[String] =
       if (enrichedUser.tickets.nonEmpty) {
         val ticketString = enrichedUser.tickets.map(printUserTicket).mkString("\n")
            Vector(
              "",
              "= Related Tickets =",
              ticketString,
              ""
            )
       } else Vector("= No related Tickets =")

       (userString +: ticketStrings).mkString("\n")
    }

    private def printUser(index: Int, user: User): String = {
      val userStrings =
        userJsonFieldNameMapping(user).map {
          case (jsonFieldName, fieldValue) =>
            val padding = getPadding(jsonFieldName.value)
            s"${jsonFieldName.value}${padding} -> ${fieldValue.value}"
        }

      userStrings.toVector.mkString(
        s"\n== User ${index.toString} ==\n",
        "\n",
        ""
      )
    }

    private def printUserTicket(ticket: Ticket): String = {
      val ticketId = ticket.id.value
      val ticketSubject = ticket.subject.map(_.value).getOrElse("-")
      s"${ticketId}:${ticketSubject}"
    }

    private def getPadding(fieldName: String): String =  {
      val fillLength = Math.max(maxFieldLength - fieldName.length, 0)
      Vector.fill(fillLength)(" ").mkString
    }

    private def noResultsFound: Vector[String] =
      Vector(
        "",
        "No results found :(",
        ""
      )

    private def lengthOfLongestFieldName(fields: NonEmptyVector[JsonFieldName]): Int = {
      val lengthOfAllFieldNames = fields.map(_.value.length)
      getMaxLength(lengthOfAllFieldNames)
    }
  }
}
