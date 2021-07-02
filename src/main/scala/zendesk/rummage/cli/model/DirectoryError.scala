package zendesk.rummage.cli.model

import java.io.File

/**
 * Errors when validating a directory.
 */
sealed trait DirectoryError

object DirectoryError {
  /**
   * The directory is not a directory (could be a file or symlink etc).
   * @param file The directory that was tested
   */
  final case class NotADirectory(file: File) extends DirectoryError

  /**
   * The directory is not readable.
   * @param file The directory that was tested
   */
  final case class NotReadable(file: File) extends DirectoryError
}
