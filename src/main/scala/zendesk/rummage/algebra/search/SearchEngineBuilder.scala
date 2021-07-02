package zendesk.rummage.algebra.search

/**
 * Builds a [[zendesk.rummage.algebra.search.SearchEngine]] from various input data
 * and [[zendesk.rummage.algebra.search.DataSetProcessor]]s.
 */
trait SearchEngineBuilder {

  /**
   * Builds a [[zendesk.rummage.algebra.search.SearchEngine]]
   * @return SearchEngine that can respond to a [[zendesk.rummage.model.SearchQuery]]
   */
  def build(): SearchEngine
}

object SearchEngineBuilder {

  import zendesk.rummage.model.domain.User
  import zendesk.rummage.model.domain.Ticket
  import zendesk.rummage.model.field.UserId
  import zendesk.rummage.model.field.TicketId
  import zendesk.rummage.algebra.search.table.UserTable
  import zendesk.rummage.algebra.search.table.TicketTable

  final class LiveSearchEngineBuilder(userData: Vector[User], ticketData: Vector[Ticket]) extends SearchEngineBuilder {

    override def build(): SearchEngine = {
      val userTable   = createUserTable()
      val ticketTable = createTicketTable()
      new SearchEngine.LiveSearchEngine(userTable, ticketTable)
    }

    private def createUserTable(): UserTable = {
      val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](userData)

      val tableData   = dataSetProcessor.processDataSet()
      val fieldsIndex = dataSetProcessor.processFieldIndex()

      new UserTable.LiveUserTable(tableData, fieldsIndex)
    }

    private def createTicketTable(): TicketTable = {
      val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[TicketId, Ticket](ticketData)

      val tableData    = dataSetProcessor.processDataSet()
      val fieldsIndex  = dataSetProcessor.processFieldIndex()

      new TicketTable.LiveTicketTable(tableData, fieldsIndex)
    }
  }
}
