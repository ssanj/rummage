package zendesk.rummage.algebra.search

import org.scalatest.flatspec.AnyFlatSpec
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.field.TicketId
import zendesk.rummage.algebra.search.table.TicketTable
import zendesk.rummage.model.domain.DomainHelper._

final class LiveTicketTableSpec extends AnyFlatSpec {

  // --------------------- Ticket1 ---------------------
  private val ticketId1 = "674a19a1-c330-45fb-8b61-b4d77ba87130"
  private val createdAt1 = "2016-03-07T08:24:53-11:00"
  private val ticketType1 =  "task"
  private val subject1 = "A Drama in St. Pierre and Miquelon"
  private val assigneeId1 = 14L
  private val assigneeId1String = "14"

  private val tags1 =
    List(
      "Connecticut",
      "Arkansas",
      "Missouri",
      "Alabama"
    )

  private val tags1String = "[Connecticut,Arkansas,Missouri,Alabama]"

  private val ticket1 =
    createTicket(
      ticketId1,
      createdAt1,
      ticketType1,
      subject1,
      assigneeId1,
      tags1
    )

  // --------------------- Ticket2 ---------------------
  private val ticketId2 = "2217c7dc-7371-4401-8738-0a8a8aedc08d"
  private val createdAt2 = "2016-07-16T12:05:12-10:00"
  private val ticketType2 = "problem"
  private val subject2 = "A Catastrophe in Hungary"
  private val assigneeId2 = 65L
  private val assigneeId2String = "65"
  private val tags2 =
    List(
      "Massachusetts",
      "New York",
      "Minnesota",
      "New Jersey"
    )

  private val tags2String = "[Massachusetts,New York,Minnesota,New Jersey]"

  private val ticket2 =
    createTicket(
      ticketId2,
      createdAt2,
      ticketType2,
      subject2,
      assigneeId2,
      tags2
    )

  // --------------------- Ticket3 ---------------------
  private val ticketId3 = "35d6bb75-10fd-4ce8-8688-dde2882b623f"
  private val createdAt3 = "2016-02-02T12:58:47-11:00"
  private val ticketType3 = "question"
  private val subject3 = "A Problem in St. Helena"
  private val assigneeId3 = 65L
  private val assigneeId3String = "65"
  private val tags3 =
    List(
      "Rhode Island",
      "Kansas",
      "Guam",
      "Colorado"
    )

  private val tags3String = "[Rhode Island,Kansas,Guam,Colorado]"

  private val ticket3 =
    createTicket(
      ticketId3,
      createdAt3,
      ticketType3,
      subject3,
      assigneeId3,
      tags3
    )

  // --------------------- TableData  ---------------------
  private val tableData =
    Map(
      TicketId(ticketId1) -> ticket1,
      TicketId(ticketId2) -> ticket2,
      TicketId(ticketId3) -> ticket3
    )


  // --------------------- FieldsIndex  ---------------------
  private val fieldsIndex =
    Map(
      FieldName("id")  ->
        Map(
          FieldValue("674a19a1-c330-45fb-8b61-b4d77ba87130") -> Vector(TicketId(ticketId1)),
          FieldValue("2217c7dc-7371-4401-8738-0a8a8aedc08d") -> Vector(TicketId(ticketId2)),
          FieldValue("35d6bb75-10fd-4ce8-8688-dde2882b623f") -> Vector(TicketId(ticketId3))
        ),

      FieldName("createAt")   ->
        Map(
          FieldValue("2016-03-07T08:24:53-11:00") -> Vector(TicketId(ticketId1)),
          FieldValue("2016-07-16T12:05:12-10:00") -> Vector(TicketId(ticketId2)),
          FieldValue("2016-02-02T12:58:47-11:00") -> Vector(TicketId(ticketId3))
        ),

      FieldName("ticketType") ->
        Map(
          FieldValue("task")    -> Vector(TicketId(ticketId1)),
          FieldValue("problem") -> Vector(TicketId(ticketId2)),
          FieldValue("question") -> Vector(TicketId(ticketId3))
        ),

      FieldName("subject") ->
        Map(
          FieldValue("A Drama in St. Pierre and Miquelon") -> Vector(TicketId(ticketId1)),
          FieldValue("A Catastrophe in Hungary") -> Vector(TicketId(ticketId2)),
          FieldValue("A Problem in St. Helena") -> Vector(TicketId(ticketId3))
        ),

      FieldName("assigneeId") ->
        Map(
          FieldValue("14") -> Vector(TicketId(ticketId1)),
          FieldValue("65") -> Vector(TicketId(ticketId2), TicketId(ticketId3))
        ),

      FieldName("tags") ->
        Map(
          FieldValue("[Connecticut,Arkansas,Missouri,Alabama]")       -> Vector(TicketId(ticketId1)),
          FieldValue("[Massachusetts,New York,Minnesota,New Jersey]") -> Vector(TicketId(ticketId2)),
          FieldValue("[Rhode Island,Kansas,Guam,Colorado]")           -> Vector(TicketId(ticketId3))
        )
    )

  "A LiveTicketTable" should "find a match for a given field name and value" in {
    val ticketTable = new TicketTable.LiveTicketTable(tableData, fieldsIndex)

    assert(ticketTable.findBy(FieldName("id"),         FieldValue(ticketId1))         == Vector(ticket1), "id1")
    assert(ticketTable.findBy(FieldName("createAt"),   FieldValue(createdAt1))        == Vector(ticket1), "createAt1")
    assert(ticketTable.findBy(FieldName("subject"),    FieldValue(subject1))          == Vector(ticket1), "subject1")
    assert(ticketTable.findBy(FieldName("assigneeId"), FieldValue(assigneeId1String)) == Vector(ticket1), "assigneeId1")
    assert(ticketTable.findBy(FieldName("tags"),       FieldValue(tags1String))       == Vector(ticket1), "tags1String")

    assert(ticketTable.findBy(FieldName("id"),         FieldValue(ticketId2))         == Vector(ticket2), "id2")
    assert(ticketTable.findBy(FieldName("createAt"),   FieldValue(createdAt2))        == Vector(ticket2), "createAt2")
    assert(ticketTable.findBy(FieldName("subject"),    FieldValue(subject2))          == Vector(ticket2), "subject2")
    assert(ticketTable.findBy(FieldName("assigneeId"), FieldValue(assigneeId2String)) == Vector(ticket2, ticket3), "assigneeId2")
    assert(ticketTable.findBy(FieldName("tags"),       FieldValue(tags2String))       == Vector(ticket2), "tags2")

    assert(ticketTable.findBy(FieldName("id"),         FieldValue(ticketId3))         == Vector(ticket3), "id3")
    assert(ticketTable.findBy(FieldName("createAt"),   FieldValue(createdAt3))        == Vector(ticket3), "createAt3")
    assert(ticketTable.findBy(FieldName("subject"),    FieldValue(subject3))          == Vector(ticket3), "subject3")
    assert(ticketTable.findBy(FieldName("assigneeId"), FieldValue(assigneeId3String)) == Vector(ticket2, ticket3),  "assigneeId3")
    assert(ticketTable.findBy(FieldName("tags"),       FieldValue(tags3String))       == Vector(ticket3), "tags3")

  }

  it should "find against an empty table" in {
    val localTableData   = Map.empty[TicketId, Ticket]
    val localFieldsIndex = Map.empty[FieldName, Map[FieldValue, Vector[TicketId]]]

    val ticketTable = new TicketTable.LiveTicketTable(localTableData, localFieldsIndex)

    val results = ticketTable.findBy(FieldName("id"), FieldValue("1a227508-9f39-427c-8f57-1b72f3fab87c"))
    assert(results.isEmpty)
  }

  it should "not find a match when there is no table data for a given ticket id" in {
    val localTableData = Map.empty[TicketId, Ticket]
    val localFieldsIndex =
      Map(
        FieldName("subject") -> Map(
          FieldValue(subject2) -> Vector(TicketId(ticketId2))
        )
      )

    val ticketTable = new TicketTable.LiveTicketTable(localTableData, localFieldsIndex)
    val results = ticketTable.findBy(FieldName("subject"), FieldValue(subject2))

    assert(results.isEmpty)
  }

  it should "not find a match if there is no field name with a given value" in {
    val localTableData = Map(TicketId(ticketId1) -> ticket1)
    val localFieldsIndex = Map.empty[FieldName, Map[FieldValue, Vector[TicketId]]]

    val ticketTable = new TicketTable.LiveTicketTable(localTableData, localFieldsIndex)
    val results = ticketTable.findBy(FieldName("id"), FieldValue(ticketId1))

    assert(results.isEmpty)
  }

  it should "find a match by primary key" in {
    val ticketTable = new TicketTable.LiveTicketTable(tableData, fieldsIndex)

    assert(ticketTable.findByPrimaryKey(TicketId(ticketId1)).contains(ticket1))
    assert(ticketTable.findByPrimaryKey(TicketId(ticketId2)).contains(ticket2))
    assert(!ticketTable.findByPrimaryKey(TicketId("not a ticket id")).contains(ticket2))
  }

  it should "find by assigneeId" in {
    val ticketTable = new TicketTable.LiveTicketTable(tableData, fieldsIndex)
    assert(ticketTable.findByAssigneeId(UserId(assigneeId1)).contains(ticket1))
    assert(ticketTable.findByAssigneeId(UserId(assigneeId2)).contains(ticket2))
    assert(ticketTable.findByAssigneeId(UserId(assigneeId2)) == Vector(ticket2, ticket3))
    assert(!ticketTable.findByAssigneeId(UserId(400L)).contains(ticket1))
    assert(!ticketTable.findByAssigneeId(UserId(400L)).contains(ticket2))
  }
}
