package zendesk.rummage.model.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.Assertion
import DomainHelper._
import zendesk.rummage.model.domain.DataMapper
import zendesk.rummage.model.JsonFieldName
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.field.UserId

import cats.data.NonEmptyVector

final class UserDataMapperSpec extends AnyFlatSpec {

  private val userDataMapper = DataMapper[UserId, User]

  private val expectedJsonFieldNames =
    NonEmptyVector.of(
      JsonFieldName("_id"),
      JsonFieldName("name"),
      JsonFieldName("created_at"),
      JsonFieldName("verified"),
    )

  private val expectedDomainFieldNames =
    NonEmptyVector.of(
      FieldName("id"),
      FieldName("name"),
      FieldName("createdAt"),
      FieldName("verified"),
    )

  "A User DataMapper" should "return field mappings between JSON input fields and domain input fields" in {
    val fieldMappings = userDataMapper.fieldMappings

    val user = createUser(74, "Melissa Bishop", "2016-02-17T10:35:02-11:00", false)

    assert(fieldMappings.length == 4)
    val expectedFieldMappings =
      NonEmptyVector.of(
        (JsonFieldName("_id"),        FieldName("id"),       FieldValue("74")),
        (JsonFieldName("name"),       FieldName("name"),     FieldValue("Melissa Bishop")),
        (JsonFieldName("created_at"), FieldName("createdAt"),FieldValue("2016-02-17T10:35:02-11:00")),
        (JsonFieldName("verified"),   FieldName("verified"), FieldValue("false"))
      )

    val actualMappings = fieldMappings.map(fm => (fm.jsonField, fm.domainField, fm.getter(user)))
    assert(actualMappings.get(0) == expectedFieldMappings.get(0))
    assert(actualMappings.get(1) == expectedFieldMappings.get(1))
    assert(actualMappings.get(2) == expectedFieldMappings.get(2))
    assert(actualMappings.get(3) == expectedFieldMappings.get(3))
  }

  it should "return JSON field names" in {
    val jsonFieldNames = userDataMapper.jsonFieldNames
    assert(jsonFieldNames == expectedJsonFieldNames)
  }

  it should "return domain field names" in {
    val domainFieldNames = userDataMapper.domainFieldNames
    assert(domainFieldNames == expectedDomainFieldNames)
  }

  it should "return values by JSON field name" in {

    val user = createUser(6, "Riggs Hebert", "2016-04-04T01:30:49-10:00", false)

    val jsonFieldValueNames = userDataMapper.getJsonFieldNameAndValues(user)

    val expectedJsonFieldValues =
      NonEmptyVector.of(
        JsonFieldName("_id")        -> FieldValue("6"),
        JsonFieldName("name")       -> FieldValue("Riggs Hebert"),
        JsonFieldName("created_at") -> FieldValue("2016-04-04T01:30:49-10:00"),
        JsonFieldName("verified")   -> FieldValue("false")
      )

    assert(jsonFieldValueNames == expectedJsonFieldValues)
  }

  it should "return domain field name mapped to a JSON field name" in {
    val fieldMatches: NonEmptyVector[Boolean] =
      expectedJsonFieldNames.zipWith[FieldName, Boolean](expectedDomainFieldNames){
        case (jsonFieldName, domainFieldName) => userDataMapper.domainFieldForJsonField(jsonFieldName).contains(domainFieldName)
      }

    assert(fieldMatches.forall(identity))
  }

  it should "return JSON field name mapped to a domain field name" in {
    val fieldMatches: NonEmptyVector[Boolean] =
      expectedDomainFieldNames.zipWith[JsonFieldName, Boolean](expectedJsonFieldNames){
        case (domainFieldName, jsonFieldName) => userDataMapper.jsonFieldForDomainField(domainFieldName).contains(jsonFieldName)
      }

    assert(fieldMatches.forall(identity))
  }

  it should "return values by domain field name" in {

    val user = createUser(10, "Kari Vinson", "2016-02-08T04:32:38-11:00", false)

    val domainFieldValueNames = userDataMapper.getDomainFieldNameAndValues(user)

    val expectedDomainFieldValues =
      NonEmptyVector.of(
        FieldName("id")        -> FieldValue("10"),
        FieldName("name")      -> FieldValue("Kari Vinson"),
        FieldName("createdAt") -> FieldValue("2016-02-08T04:32:38-11:00"),
        FieldName("verified")  -> FieldValue("false")
      )

    assert(domainFieldValueNames == expectedDomainFieldValues)
  }

  it should "return a primary key" in {
    val userId = UserId(1000)
    val user = createSimpleUser(1000)

    val pk = userDataMapper.getPrimaryKey(user)
    assert(pk == userId)
  }

it should "return the field names and values linked by user id" in {

    val userId = 1L
    val user = createUser(userId, "Francisca Rasmussen", "2016-04-15T05:19:46-10:00", true)

    val fieldNamesAndValues = userDataMapper.getFieldNameAndValues(user)

    assert(fieldNamesAndValues.length == 4)
    val expectedMappings =
      NonEmptyVector.of(
        "id"        -> ("1"                         -> userId),
        "name"      -> ("Francisca Rasmussen"       -> userId),
        "createdAt" -> ("2016-04-15T05:19:46-10:00" -> userId),
        "verified"  -> ("true"                      -> userId)
      )

    assertFieldNameAndValuePKMapping(expectedMappings)(fieldNamesAndValues)

    val fieldNamesAndValuesMap = fieldNamesAndValues.toVector.toMap
    assert(fieldNamesAndValuesMap.get(FieldName("some unknown field")) == None)
  }

  it should "return the field names with optional fields values mapped to empty String" in {
    val userId = 1234L
    val user = createSimpleUser(userId)

    val fieldNamesAndValues = userDataMapper.getFieldNameAndValues(user)

    assert(fieldNamesAndValues.length == 4)
    val expectedMappings =
      NonEmptyVector.of(
        "id"        -> ("1234" -> userId),
        "name"      -> (""     -> userId),
        "createdAt" -> (""     -> userId),
        "verified"  -> (""     -> userId)
      )

    assertFieldNameAndValuePKMapping(expectedMappings)(fieldNamesAndValues)
  }

  private def assertFieldNameAndValuePKMapping(expected: NonEmptyVector[(String, (String, Long))])(actualMappings: NonEmptyVector[(FieldName, (FieldValue, UserId))]): Assertion = {
    val expectedMappings = expected.map {
      case (fieldName, (fieldValue, pk)) => (FieldName(fieldName), (FieldValue(fieldValue), UserId(pk)))
    }

    assert(expectedMappings == actualMappings)
  }

}



