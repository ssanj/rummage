package zendesk.rummage.cli.model

import java.io.File

/**
 * A marker for a validated directory.
 * @param dataDirectory The directory that is valid
 * @see [[zendesk.rummage.cli.algebra.DirectoryValidator]]
 */
final case class ValidDataDirectory(dataDirectory: File)
