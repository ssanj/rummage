package zendesk.rummage.algebra.search

import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue
import zendesk.rummage.algebra.search.table.TableData
import zendesk.rummage.algebra.search.table.FieldsIndex
import zendesk.rummage.model.domain.DataMapper

/**
 * Processes data for ease of searching.
 * The [[zendesk.rummage.algebra.search.DataSetProcessor]] are meant to be used with the [[zendesk.rummage.algebra.search.SearchEngineBuilder]]
 * which will then create a [[zendesk.rummage.algebra.search.SearchEngine]] with the required inputs.
 *
 * @see [[zendesk.rummage.algebra.search.SearchEngine]]
 * @see [[zendesk.rummage.algebra.search.SearchEngineBuilder]]
 * @see [[zendesk.rummage.algebra.search.DataSetProcessor.DefaultDataSetProcessor]]
 * @tparam PK The primary key for the domain object
 * @tparam Domain The domain object
 */
trait DataSetProcessor[PK, Domain] {

  /**
   * Creates a `Map` keyed by the primary key type (`PK`) for values of the domain object `Domain`.
   * @return [[zendesk.rummage.algebra.search.table.TableData]]
   */
  def processDataSet(): TableData[PK, Domain]

  /**
   * Creates a "index" of all data fields in the `Domain` object by field name and then by field value which maps onto
   * a `Vector[PK]` that match the field name with the field value.
   * @return [[zendesk.rummage.algebra.search.table.FieldsIndex]] that maps between a field name, its value and any matching `Domain` objects with that `PK`.
   */
  def processFieldIndex(): FieldsIndex[PK]
}

object DataSetProcessor {

  final class DefaultDataSetProcessor[PK, Domain](data: Vector[Domain])(implicit dataMapper: DataMapper[PK, Domain]) extends DataSetProcessor[PK, Domain] {

    final override def processDataSet(): TableData[PK, Domain] = {
        data.map(v => (dataMapper.getPrimaryKey(v), v)).toMap
    }

    final override def processFieldIndex(): FieldsIndex[PK] = {
      //Get the field mappings each domain instance
      val fieldNamesAndValueMappingsForEach: Vector[Vector[(FieldName, (FieldValue, PK))]] =
        data.map(domain => dataMapper.getFieldNameAndValues(domain).toVector)

      //Get one collection with all the mappings
      val fieldNamesAndValueMappings: Vector[(FieldName, (FieldValue, PK))] = fieldNamesAndValueMappingsForEach.flatten

      //Index values by field name to field value + PK
      val fieldNameByFieldValueAndPKMap : Map[FieldName, Vector[(FieldValue, PK)]] = fieldNamesAndValueMappings.groupMap({
        case (fn, _) => fn
      })({
        case (_, fvPairs) => fvPairs
      })

      //Index field values by field value to collection of matching PKs
      val fieldNameByFieldValueToPKMapMap: Map[FieldName, Map[FieldValue, Vector[PK]]] = fieldNameByFieldValueAndPKMap.view.mapValues(v => {
        v.groupMap({
          case (fv, _) => fv
        })({
          case (_, id) => id
        })
      }).toMap

      //MapMap? Surely there must be a better suffix. It escapes me at the moment
      fieldNameByFieldValueToPKMapMap
    }
  }
}


