package zendesk.rummage.model.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Assertion
import DomainHelper._
import zendesk.rummage.model.JsonFieldName
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.field.TicketId

import cats.data.NonEmptyVector

final class TicketDataMapperSpec extends AnyFlatSpec {

  val ticketDataMapper = DataMapper[TicketId, Ticket]

  val expectedDomainFieldNames =
    NonEmptyVector.of(
      FieldName("id"),
      FieldName("createdAt"),
      FieldName("ticketType"),
      FieldName("subject"),
      FieldName("assigneeId"),
      FieldName("tags")
    )

  val expectedJsonFieldNames =
    NonEmptyVector.of(
      JsonFieldName("_id"),
      JsonFieldName("created_at"),
      JsonFieldName("type"),
      JsonFieldName("subject"),
      JsonFieldName("assignee_id"),
      JsonFieldName("tags")
    )

  "A Ticket DataMapper" should "return field mappings between json input fields and domain input fields" in {
    val fieldMappings = ticketDataMapper.fieldMappings
    val ticket =
      createTicket(
        "674a19a1-c330-45fb-8b61-b4d77ba87130",
        "2016-03-07T08:24:53-11:00",
        "task",
        "A Drama in St. Pierre and Miquelon",
        14L,
        List(
          "Connecticut",
          "Arkansas",
          "Missouri",
          "Alabama"
        )
      )

    assert(fieldMappings.length == 6)
    val expectedFieldMappings =
      NonEmptyVector.of(
        (JsonFieldName("_id"),         FieldName("id"),         FieldValue("674a19a1-c330-45fb-8b61-b4d77ba87130")),
        (JsonFieldName("created_at"),  FieldName("createdAt"),  FieldValue("2016-03-07T08:24:53-11:00")),
        (JsonFieldName("type"),        FieldName("ticketType"), FieldValue("task")),
        (JsonFieldName("subject"),     FieldName("subject"),    FieldValue("A Drama in St. Pierre and Miquelon")),
        (JsonFieldName("assignee_id"), FieldName("assigneeId"), FieldValue("14")),
        (JsonFieldName("tags"),        FieldName("tags"),       FieldValue("[Connecticut,Arkansas,Missouri,Alabama]"))
      )

    val actualMappings = fieldMappings.map(fm => (fm.jsonField, fm.domainField, fm.getter(ticket)))
    assert(actualMappings.get(0) == expectedFieldMappings.get(0))
    assert(actualMappings.get(1) == expectedFieldMappings.get(1))
    assert(actualMappings.get(2) == expectedFieldMappings.get(2))
    assert(actualMappings.get(3) == expectedFieldMappings.get(3))
  }

  it should "return json field names" in {
    val jsonFieldNames = ticketDataMapper.jsonFieldNames
    assert(jsonFieldNames == expectedJsonFieldNames)
  }

  it should "return domain field names" in {
    val domainFieldNames = ticketDataMapper.domainFieldNames
    assert(domainFieldNames == expectedDomainFieldNames)
  }

  it should "return values by json field name" in {

    val ticket =
      createTicket(
        "c73a0be5-e967-4948-b0a4-eff98d1a43ad",
        "2016-06-12T09:32:30-10:00",
        "problem",
        "A Catastrophe in Maldives",
        34L,
        List(
          "Virginia",
          "Virgin Islands",
          "Maine",
          "West Virginia"
        )
      )

    val jsonFieldValueNames = ticketDataMapper.getJsonFieldNameAndValues(ticket)

    val expectedJsonFieldValues =
      NonEmptyVector.of(
        JsonFieldName("_id")         -> FieldValue("c73a0be5-e967-4948-b0a4-eff98d1a43ad"),
        JsonFieldName("created_at")  -> FieldValue("2016-06-12T09:32:30-10:00"),
        JsonFieldName("type")        -> FieldValue("problem"),
        JsonFieldName("subject")     -> FieldValue("A Catastrophe in Maldives"),
        JsonFieldName("assignee_id") -> FieldValue("34"),
        JsonFieldName("tags")        -> FieldValue("[Virginia,Virgin Islands,Maine,West Virginia]")
      )

    assert(jsonFieldValueNames == expectedJsonFieldValues)
  }

  it should "return domain field name mapped to a JSON field name" in {
    val fieldMatches: NonEmptyVector[Boolean] =
      expectedJsonFieldNames.zipWith[FieldName, Boolean](expectedDomainFieldNames){
        case (jsonFieldName, domainFieldName) => ticketDataMapper.domainFieldForJsonField(jsonFieldName).contains(domainFieldName)
      }

    assert(fieldMatches.forall(identity))
  }

  it should "return JSON field name mapped to a domain field name" in {
    val fieldMatches: NonEmptyVector[Boolean] =
      expectedDomainFieldNames.zipWith[JsonFieldName, Boolean](expectedJsonFieldNames){
        case (domainFieldName, jsonFieldName) => ticketDataMapper.jsonFieldForDomainField(domainFieldName).contains(jsonFieldName)
      }

    assert(fieldMatches.forall(identity))
  }

  it should "return values by domain field name" in {

    val ticket =
      createTicket(
        "35d6bb75-10fd-4ce8-8688-dde2882b623f",
        "2016-02-02T12:58:47-11:00",
        "question",
        "A Problem in St. Helena",
        65L,
        List(
          "Rhode Island",
          "Kansas",
          "Guam",
          "Colorado"
        )
      )

    val domainFieldValueNames = ticketDataMapper.getDomainFieldNameAndValues(ticket)

    val expectedDomainFieldValues =
      NonEmptyVector.of(
        FieldName("id")         -> FieldValue("35d6bb75-10fd-4ce8-8688-dde2882b623f"),
        FieldName("createdAt")  -> FieldValue("2016-02-02T12:58:47-11:00"),
        FieldName("ticketType") -> FieldValue("question"),
        FieldName("subject")    -> FieldValue("A Problem in St. Helena"),
        FieldName("assigneeId") -> FieldValue("65"),
        FieldName("tags")       -> FieldValue("[Rhode Island,Kansas,Guam,Colorado]")
      )

    assert(domainFieldValueNames == expectedDomainFieldValues)
  }

  it should "return a primary key" in {
    val ticketId = TicketId("2217c7dc-7371-4401-8738-0a8a8aedc08d")
    val ticket = createSimpleTicket("2217c7dc-7371-4401-8738-0a8a8aedc08d")

    val pk = ticketDataMapper.getPrimaryKey(ticket)
    assert(pk == ticketId)
  }

  it should "return the field names and values linked by ticket id" in {
    val ticketId = "50f3fdbd-f8a6-481d-9bf7-572972856628"
    val ticket =
      createTicket(
        ticketId,
        "2016-05-19T08:52:06-10:00",
        "incident",
        "A Nuisance in Namibia",
        12L,
        List(
          "Maine",
          "West Virginia",
          "Michigan",
          "Florida"
        )
      )

    val fieldNamesAndValues = ticketDataMapper.getFieldNameAndValues(ticket)

    assert(fieldNamesAndValues.length == 6)

    val expectedMapping =
      NonEmptyVector.of(
        "id"         -> ("50f3fdbd-f8a6-481d-9bf7-572972856628"   -> ticketId),
        "createdAt"  -> ("2016-05-19T08:52:06-10:00"              -> ticketId),
        "ticketType" -> ("incident"                               -> ticketId),
        "subject"    -> ("A Nuisance in Namibia"                  -> ticketId),
        "assigneeId" -> ("12"                                     -> ticketId),
        "tags"       -> ("[Maine,West Virginia,Michigan,Florida]" -> ticketId)
      )


    assertFieldNameAndValuePKMapping(expectedMapping)(fieldNamesAndValues)

    val fieldNamesAndValuesMap = fieldNamesAndValues.toVector.toMap
    assert(fieldNamesAndValuesMap.get(FieldName("some unknown field")) == None)
  }

  it should "return the field names with optional fields values mapped to empty String" in {
    val ticketId = "cb7cae87-2915-44d4-bda4-4ccb59c63bd4"
    val ticket = createSimpleTicket(ticketId)


    val fieldNamesAndValues = ticketDataMapper.getFieldNameAndValues(ticket)

    assert(fieldNamesAndValues.length == 6)

    val expectedMapping =
      NonEmptyVector.of(
        "id"         -> ("cb7cae87-2915-44d4-bda4-4ccb59c63bd4" -> ticketId),
        "createdAt"  -> (""                                     -> ticketId),
        "ticketType" -> (""                                     -> ticketId),
        "subject"    -> (""                                     -> ticketId),
        "assigneeId" -> (""                                     -> ticketId),
        "tags"       -> (""                                     -> ticketId)
      )

    assertFieldNameAndValuePKMapping(expectedMapping)(fieldNamesAndValues)
  }

  private def assertFieldNameAndValuePKMapping(expected: NonEmptyVector[(String, (String, String))])(actualMappings: NonEmptyVector[(FieldName, (FieldValue, TicketId))]): Assertion = {
    val expectedMappings = expected.map {
      case (fieldName, (fieldValue, pk)) => (FieldName(fieldName), (FieldValue(fieldValue), TicketId(pk)))
    }

    assert(expectedMappings == actualMappings)
  }

}



