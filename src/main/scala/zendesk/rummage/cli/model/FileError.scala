package zendesk.rummage.cli.model

/**
 * Types of file errors.
 */
sealed trait FileError
object FileError {
  /**
   * File could not be loaded.
   * @param file The file that was attempted
   * @param reason The reason for the failure
   * @param error An optional Exception
   */
  final case class CouldNotLoadFile(file: String, reason: String, error: Option[Throwable]) extends FileError
}
