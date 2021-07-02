package zendesk.rummage.algebra.search

import org.scalatest.flatspec.AnyFlatSpec
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.model.domain.User
import zendesk.rummage.model.field.UserId
import zendesk.rummage.algebra.search.table.UserTable
import zendesk.rummage.model.domain.DomainHelper._

final class LiveUserTableSpec extends AnyFlatSpec {

  // --------------------- User1 ---------------------
  private val name1 = "Josefa Mcfadden"
  private val createdAt1 = "2016-03-26T04:11:05-11:00"
  private val userId1 = 9L
  private val userId1String = "9"
  private val verified1 = false
  private val verified1String = "false"
  private val user1 = createUser(userId1, name1, createdAt1, verified1)


  // --------------------- User2 ---------------------
  private val name2 = "Terri Mcmahon"
  private val createdAt2 = "2016-01-05T08:35:03-11:00"
  private val userId2 = 28L
  private val userId2String = "28"
  private val verified2 = true
  private val verified2String = "true"
  private val user2 = createUser(userId2, name2, createdAt2, verified2)

  // --------------------- TableData ---------------------
  private val tableData =
    Map(
      UserId(userId1) -> user1,
      UserId(userId2) -> user2
    )

  // --------------------- FieldsIndex ---------------------
  private val fieldsIndex =
    Map(
      FieldName("id") ->
        Map(
         FieldValue("9")  -> Vector(UserId(userId1)),
         FieldValue("28") -> Vector(UserId(userId2))
        ),

      FieldName("name") ->
        Map(
         FieldValue("Josefa Mcfadden") -> Vector(UserId(userId1)),
         FieldValue("Terri Mcmahon")   -> Vector(UserId(userId2))
        ),

      FieldName("createdAt") ->
        Map(
         FieldValue("2016-03-26T04:11:05-11:00") -> Vector(UserId(userId1)),
         FieldValue("2016-01-05T08:35:03-11:00") -> Vector(UserId(userId2))
        ),

      FieldName("verified") ->
        Map(
         FieldValue("false") -> Vector(UserId(userId1)),
         FieldValue("true")  -> Vector(UserId(userId2))
        )
    )

  "A LiveUserTable" should "find a match for a given field name and value" in {
    val userTable = new UserTable.LiveUserTable(tableData, fieldsIndex)
    assert(userTable.findBy(FieldName("id"),         FieldValue(userId1String))    == Vector(user1), "id1")
    assert(userTable.findBy(FieldName("name"),       FieldValue(name1))            == Vector(user1), "name1")
    assert(userTable.findBy(FieldName("createdAt"),  FieldValue(createdAt1))       == Vector(user1), "createdAt1")
    assert(userTable.findBy(FieldName("verified"),   FieldValue(verified1String))  == Vector(user1), "verified1")

    assert(userTable.findBy(FieldName("id"),         FieldValue(userId2String))    == Vector(user2), "id2")
    assert(userTable.findBy(FieldName("name"),       FieldValue(name2))            == Vector(user2), "name2")
    assert(userTable.findBy(FieldName("createdAt"),  FieldValue(createdAt2))       == Vector(user2), "createdAt2")
    assert(userTable.findBy(FieldName("verified"),   FieldValue(verified2String))  == Vector(user2), "verified2")
  }

  it should "find against an empty table" in {
    val localTableData = Map.empty[UserId, User]
    val localFieldsIndex = Map.empty[FieldName, Map[FieldValue, Vector[UserId]]]

    val userTable = new UserTable.LiveUserTable(localTableData, localFieldsIndex)
    val results = userTable.findBy(FieldName("name"), FieldValue("Terri Mcmahon"))

    assert(results.isEmpty)
  }

  it should "not find a match when there is no data for a given user id" in {
    val localTableData = Map.empty[UserId, User]
    val localFieldsIndex =
      Map(
        FieldName("name") -> Map(
          FieldValue("Terri Mcmahon") -> Vector(UserId(userId2))
        )
      )

    val userTable = new UserTable.LiveUserTable(localTableData, localFieldsIndex)
    val results = userTable.findBy(FieldName("name"), FieldValue("Terri Mcmahon"))

    assert(results.isEmpty)
  }

  it should "not find a match if there is no field name with a given value" in {
    val localTableData = Map(UserId(9) -> user1)
    val localFieldsIndex = Map.empty[FieldName, Map[FieldValue, Vector[UserId]]]

    val userTable = new UserTable.LiveUserTable(localTableData, localFieldsIndex)
    val results = userTable.findBy(FieldName("name"), FieldValue("Josefa Mcfadden"))

    assert(results.isEmpty)
  }

  it should "find a match by primary key" in {
    val userTable = new UserTable.LiveUserTable(tableData, fieldsIndex)

    assert(userTable.findByPrimaryKey(UserId(userId1)).contains(user1))
    assert(userTable.findByPrimaryKey(UserId(userId2)).contains(user2))
    assert(!userTable.findByPrimaryKey(UserId(1000)).contains(user2))
    assert(!userTable.findByPrimaryKey(UserId(1000)).contains(user1))
  }

}
