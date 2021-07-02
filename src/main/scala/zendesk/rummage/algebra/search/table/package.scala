package zendesk.rummage.algebra.search

import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue

package object table {

  type TableData[PK, Domain] = Map[PK, Domain]

  type FieldsIndex[PK] = Map[FieldName, Map[FieldValue, Vector[PK]]]
}
