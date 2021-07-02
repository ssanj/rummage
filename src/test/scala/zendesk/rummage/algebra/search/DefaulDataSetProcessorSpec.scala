package zendesk.rummage.algebra.search

import org.scalatest.flatspec.AnyFlatSpec
import zendesk.rummage.model.domain.User
import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.domain.DomainHelper._

final class DefaultDataSetProcessorSpec extends AnyFlatSpec {

  "A DefaultDataSetProcessor" should "create a map of UserId to User" in {
    val user1 = createUser(1, "Francisca Rasmussen", "2016-04-15T05:19:46-10:00", true)
    val user2 = createUser(2, "Cross Barlow", "2016-06-23T10:31:39-10:00", true)
    val user3 = createUser(3, "Ingrid Wagner", "2016-07-28T05:29:25-10:00", false)

    val data: Vector[User] = Vector(user1, user2, user3)

    val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](data)
    val userIdToUserMap = dataSetProcessor.processDataSet()

    assert(userIdToUserMap.size == 3)
    assert(userIdToUserMap.get(UserId(1)).contains(user1))
    assert(userIdToUserMap.get(UserId(2)).contains(user2))
    assert(userIdToUserMap.get(UserId(3)).contains(user3))

    assert(userIdToUserMap.get(UserId(4)) == None) //negative test, test for something that should fail
  }

  it should "create a map of field name to field values" in {
    val user1 = createUser(1, "Francisca Rasmussen", "2016-04-15T05:19:46-10:00", true)
    val user2 = createUser(2, "Cross Barlow", "2016-06-23T10:31:39-10:00", true)
    val user3 = createUser(3, "Ingrid Wagner", "2016-07-28T05:29:25-10:00", false)
    val user4 = createUser(4, "Cross Barlow", "2016-07-28T05:30:25-10:00", false) //duplicate name

    val data = Vector(user1, user2, user3, user4)
    val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](data)

    val fieldNameToValuesMap: Map[FieldName, Map[FieldValue, Vector[UserId]]] = dataSetProcessor.processFieldIndex()

    assert(fieldNameToValuesMap.size == 4)
    val withFieldValueMap = byFieldNameAndValue(fieldNameToValuesMap) _
    val withFieldMap = byFieldName(fieldNameToValuesMap) _

    //by id
    assert(withFieldMap("id").map(_.size).contains(4))
    assert(withFieldValueMap("id", "1").contains(Vector(UserId(1))))
    assert(withFieldValueMap("id", "2").contains(Vector(UserId(2))))
    assert(withFieldValueMap("id", "3").contains(Vector(UserId(3))))
    assert(withFieldValueMap("id", "4").contains(Vector(UserId(4))))

    //by name
    assert(withFieldMap("name").map(_.size).contains(3))
    assert(withFieldValueMap("name", "Francisca Rasmussen").contains(Vector(UserId(1))))
    assert(withFieldValueMap("name", "Cross Barlow").contains(Vector(UserId(2), UserId(4))))//dupes are grouped
    assert(withFieldValueMap("name", "Ingrid Wagner").contains(Vector(UserId(3))))

    //by createdAt
    assert(withFieldMap("createdAt").map(_.size).contains(4))
    assert(withFieldValueMap("createdAt", "2016-04-15T05:19:46-10:00").contains(Vector(UserId(1))))
    assert(withFieldValueMap("createdAt", "2016-06-23T10:31:39-10:00").contains(Vector(UserId(2))))
    assert(withFieldValueMap("createdAt", "2016-07-28T05:29:25-10:00").contains(Vector(UserId(3))))
    assert(withFieldValueMap("createdAt", "2016-07-28T05:30:25-10:00").contains(Vector(UserId(4))))

    //by verified
    assert(withFieldMap("verified").map(_.size).contains(2))
    assert(withFieldValueMap("verified", "true").contains(Vector(UserId(1), UserId(2))))  //dupes are grouped
    assert(withFieldValueMap("verified", "false").contains(Vector(UserId(3), UserId(4)))) //dupes are grouped

  }

  it should "handle an empty data set" in {
    val data = Vector.empty[User]
    val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](data)
    val userIdToUserMap = dataSetProcessor.processDataSet()
    val fieldNameToFieldValueAndPKMap = dataSetProcessor.processFieldIndex()

    assert(userIdToUserMap.isEmpty)
    assert(fieldNameToFieldValueAndPKMap.isEmpty)
  }

  private def byFieldNameAndValue(fieldNameToValuesMap: Map[FieldName, Map[FieldValue, Vector[UserId]]])(
    fieldName: String, fieldValue: String): Option[Vector[UserId]] = for {
     valuesMap <- fieldNameToValuesMap.get(FieldName(fieldName))
     matches <- valuesMap.get(FieldValue(fieldValue))
  } yield matches

  private def byFieldName(fieldNameToValuesMap: Map[FieldName, Map[FieldValue, Vector[UserId]]])(
    fieldName: String): Option[Map[FieldValue, Vector[UserId]]] =  fieldNameToValuesMap.get(FieldName(fieldName))

}
