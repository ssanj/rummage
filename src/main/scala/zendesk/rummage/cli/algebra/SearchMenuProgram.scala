package zendesk.rummage.cli.algebra

import zendesk.rummage.algebra.search.SearchEngine
import zendesk.rummage.model.JsonFieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.TableType
import zendesk.rummage.cli.model.Continuation
import zendesk.rummage.model.SearchQuery
import zendesk.rummage.model.domain.DataMapper


trait SearchMenuProgram[F[_]] {

  def fieldNameDescription: List[String]

  def getFieldsNamesFor(tableType: TableType): List[String]

  def selectFieldNameString: List[String]

  def selectFieldValueString: List[String]

  def searchCriteriaString(tableType: TableType, fieldName: JsonFieldName, fieldValue: FieldValue): List[String]

  def run(tableType: TableType): F[Continuation]

  def createSearchQuery(tableType: TableType, jsonFieldName: JsonFieldName, fieldValue: FieldValue): SearchQuery
}

object SearchMenuProgram {

  import cats.Monad
  import cats.implicits._

  import zendesk.rummage.model.domain.User
  import zendesk.rummage.model.domain.Ticket
  import zendesk.rummage.model.field.UserId
  import zendesk.rummage.model.field.TicketId

  import scala.io.AnsiColor.BOLD
  import scala.io.AnsiColor.RESET

  final class LiveSearchMenuProgram[F[_]: Monad](console: Console[F], searchEngine: SearchEngine, searchResultsPrinter: SearchResultsPrinter) extends SearchMenuProgram[F] {

    override def searchCriteriaString(tableType: TableType, fieldName: JsonFieldName, fieldValue: FieldValue): List[String] =
      List(
        "",
        s"Searching ${TableType.simpleString(tableType)} for `${fieldName.value}` of `${fieldValue.value}`",
        ""
      )

    override def fieldNameDescription: List[String] =
      List(
        "",
        "You can search on the following fields:"
      )

    override def selectFieldNameString: List[String] =
      List(
        "",
        "Please enter the field name you want to search by"
      )

    override def selectFieldValueString: List[String] =
      List(
        "",
        "Please enter the field value you want to match on"
      )

    override def getFieldsNamesFor(tableType: TableType): List[String] = tableType match {
      case TableType.UserTableType   => DataMapper[UserId, User].jsonFieldNames.map(_.value).map(v => s"${BOLD}${v}${RESET}").toList
      case TableType.TicketTableType => DataMapper[TicketId, Ticket].jsonFieldNames.map(_.value).map(v => s"${BOLD}${v}${RESET}").toList
    }

    override def run(tableType: TableType): F[Continuation] = {
      for {
          _ <- console.writeLn(fieldNameDescription.mkString("\n"))
          _ <- console.writeLn(getFieldsNamesFor(tableType).mkString(" "))
          _ <- console.writeLn(selectFieldNameString.mkString("\n"))
          fieldName <- console.readLn().map(JsonFieldName.apply)
          _ <- console.writeLn(selectFieldValueString.mkString("\n"))
          fieldValue <- console.readLn().map(FieldValue)
          _ <- console.writeLn(searchCriteriaString(tableType, fieldName, fieldValue).mkString("\n"))
          searchQuery = createSearchQuery(tableType, fieldName, fieldValue)
          searchResult = searchEngine.performSearch(searchQuery)
          searchResultOutput = searchResultsPrinter.print(searchResult)
          _ <- console.writeLn(searchResultOutput.mkString("\n"))
      } yield Continuation.Stay
    }

    override def createSearchQuery(tableType: TableType, jsonFieldName: JsonFieldName, fieldValue: FieldValue): SearchQuery = {
      SearchQuery(tableType, jsonFieldName, fieldValue)
    }
  }
}
