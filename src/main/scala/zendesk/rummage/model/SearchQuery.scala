package zendesk.rummage.model

/**
 * A query entered by the user which specifies a table, field and a value.
 * @param tableType The type of table
 * @param fieldName The JSON input field name
 * @param fieldValue The JSON input field value
 */
final case class SearchQuery(tableType: TableType,  fieldName: JsonFieldName, fieldValue: FieldValue)
