package zendesk.rummage.algebra.search.table

import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue

/**
 * Represents a conceptual in-memory "table".
 * Each table type extends [[zendesk.rummage.algebra.search.table.Table]] with the respective domain object
 */
trait Table[PK, Domain] {

  /**
   * Finds matches for a given domain type `Domain` based on field name and value.
   * @param fieldName The name of the field to search across
   * @param fieldValue The value of the field
   * @tparam PK The primary key type of the `Domain`
   * @tparam Domain The `Domain` type
   * @return Any matches for field name with the respective value
   */
  def findBy(fieldName: FieldName, fieldValue: FieldValue): Vector[Domain]

  /**
   * Finds a `Domain` object (if any) by primary `PK`
   * @param Primary key to search by
   * @return Domain object if one exists
   */
  def findByPrimaryKey(pk: PK): Option[Domain]
}

object Table {
  final class GenericTable[PK, Domain](tableData: TableData[PK, Domain], fieldsIndex: FieldsIndex[PK]) extends Table[PK, Domain] {

    override def findBy(fieldName: FieldName, fieldValue: FieldValue): Vector[Domain] = {
      val opResult =
        for {
          valuesMap     <- fieldsIndex.get(fieldName)
          matchedValues <- valuesMap.get(fieldValue)
          results = matchedValues.map(tableData.get).flatten
        } yield results

      opResult.fold(Vector.empty[Domain])(identity)
    }

    override def findByPrimaryKey(pk: PK): Option[Domain] = tableData.get(pk)
  }
}
