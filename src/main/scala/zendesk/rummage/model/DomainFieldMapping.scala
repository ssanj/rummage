package zendesk.rummage.model

/**
 * Mapping between a [[zendesk.rummage.model.JsonFieldName]], [[zendesk.rummage.model.FieldName]] and its corresponding getter for
 * the value of that field.
 */
final case class DomainFieldMapping[Domain](jsonField: JsonFieldName, domainField: FieldName, getter: Domain => FieldValue)
