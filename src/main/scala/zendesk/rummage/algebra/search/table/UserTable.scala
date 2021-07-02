package zendesk.rummage.algebra.search.table

import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.domain.User
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue

trait UserTable extends Table[UserId, User]

object UserTable {

  import zendesk.rummage.model.domain.User
  import zendesk.rummage.model.field.UserId

  final class LiveUserTable(tableData: TableData[UserId, User], fieldsIndex: FieldsIndex[UserId]) extends UserTable {

    private val genericTable: Table.GenericTable[UserId, User] = new Table.GenericTable(tableData, fieldsIndex)

    override def findBy(fieldName: FieldName, fieldValue: FieldValue): Vector[User] = {
      genericTable.findBy(fieldName, fieldValue)
    }

    override def findByPrimaryKey(userId: UserId): Option[User] = genericTable.findByPrimaryKey(userId)
  }

}
