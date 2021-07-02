package zendesk.rummage.model.domain

import zendesk.rummage.model.DomainFieldMapping
import zendesk.rummage.model.JsonFieldName
import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue

import cats.data.NonEmptyVector

/**
 * Class that describes how domain fields are mapped to JSON input fields.
 * @tparam PK The primary key type for the domain
 * @tparam Domain The domain object type
 *
 * @param fieldMappings The mappings between JSON fields, domain fields and their domain values encapsulated in a
 * [[zendesk.rummage.model.DomainFieldMapping]]
 * @param getPrimaryKey Function to retrieve the primary key from a domain object
 *
 * @note The `fieldMappings` have to be updated either when the JSON input field names or the domain field names change.
 */
final class DataMapper[PK, Domain] private (val fieldMappings: NonEmptyVector[DomainFieldMapping[Domain]], val getPrimaryKey: Domain => PK) {

  /**
   * Given a `Domain` object` returns the collection of [[zendesk.rummage.model.JsonFieldName]] to [[zendesk.rummage.model.FieldValue]]
   * mappings.
   * @param value `Domain` object
   * @return Collection of pairs of [[zendesk.rummage.model.JsonFieldName and [[zendesk.rummage.model.FieldValue]].
   */
  def getJsonFieldNameAndValues(value: Domain): NonEmptyVector[(JsonFieldName, FieldValue)] = {
    fieldMappings.map(fm => (fm.jsonField, fm.getter(value)))
  }

  /**
   * Given a `Domain` object` returns the collection of [[zendesk.rummage.model.FieldName]] to [[zendesk.rummage.model.FieldValue]]
   *
   * @param value `Domain` object
   * @return Mappings between a [[zendesk.rummage.model.FieldName]] and its [[zendesk.rummage.model.FieldValue]].
   */
  def getDomainFieldNameAndValues(value: Domain): NonEmptyVector[(FieldName, FieldValue)] = {
    fieldMappings.map(fm => (fm.domainField, fm.getter(value)))
  }

  //Create this Map once and reuse
  private val jsonToDomainFieldMap: Map[JsonFieldName, FieldName] =
    Map.from[JsonFieldName, FieldName](fieldMappings.map(fm => fm.jsonField -> fm.domainField).toVector)

  //Create this Map once and reuse
  private val domainToJsonFieldMap: Map[FieldName, JsonFieldName] =
    Map.from[FieldName, JsonFieldName](fieldMappings.map(fm => fm.domainField -> fm.jsonField).toVector)

  /**
   * Returns a matching domain field name for a given JSON field name - if any.
   * @param jsonField [[zendesk.rummage.model.JsonFieldName]]
   * @return Optional [[zendesk.rummage.model.FieldName]]
   */
  def domainFieldForJsonField(jsonField: JsonFieldName): Option[FieldName] = jsonToDomainFieldMap.get(jsonField)

  /**
   * Returns a matching JSON field name for a given domain field name - if any.
   * @param domainField [[zendesk.rummage.model.FieldName]]
   * @return Optional [[zendesk.rummage.model.JsonFieldName]]
   */
  def jsonFieldForDomainField(domainField: FieldName): Option[JsonFieldName] = domainToJsonFieldMap.get(domainField)

  /**
   * Given a `Domain` object` returns the collection of [[zendesk.rummage.model.FieldName]] to [[zendesk.rummage.model.FieldValue]]
   * mappings along with `PK` for each entry.
   * @param value `Domain` object
   * @return Mappings between a [[zendesk.rummage.model.FieldName]] and a pair of [[zendesk.rummage.model.FieldValue]]s and `PK`.
   */
  def getFieldNameAndValues(value: Domain): NonEmptyVector[(FieldName, (FieldValue, PK))] = {
      val id = getPrimaryKey(value)
      fieldMappings.map(fm => (fm.domainField, (fm.getter(value), id)))
    }

  /**
   * All the JSON field names for the `Domain` object.
   */
  val jsonFieldNames: NonEmptyVector[JsonFieldName] = fieldMappings.map(fm => fm.jsonField)

  /**
   * All the domain field names for the `Domain` object.
   */
  val domainFieldNames: NonEmptyVector[FieldName] = fieldMappings.map(fm => fm.domainField)
}

object DataMapper {

  /**
   * Manifests a given [[zendesk.rummage.model.domain.DataMapper]] given `PK` and `Domain` types - if one is in implicit scope.
   * @param dm The implicit [[zendesk.rummage.model.domain.DataMapper]] instance
   * @tparam PK The primary key type
   * @tparam Domain The domain type
   * @return Instance of a matching [[zendesk.rummage.model.domain.DataMapper]]
   */
  def apply[PK, Domain](implicit dm: DataMapper[PK, Domain]): DataMapper[PK, Domain] = dm

  /**
   * Creates a [[zendesk.rummage.model.domain.DataMapper]] instance from collection of field mappings and primary key function.
   * @tparam PK The primary key type
   * @tparam Domain The domain type
   *
   * @param mappings The mappings between JSON fields, domain fields
   * @param pk The function to get the primary key of a domain object
   * @see [[zendesk.rummage.model.DomainFieldMapping]]
   */
  def from[PK, Domain](mappings: NonEmptyVector[DomainFieldMapping[Domain]], pk: Domain => PK): DataMapper[PK, Domain] =
    new DataMapper[PK, Domain](mappings, pk)
}
