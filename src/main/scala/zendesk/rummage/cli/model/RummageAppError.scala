package zendesk.rummage.cli.model

import scala.util.control.NoStackTrace

/**
 * Application-level error for the cli application.
 */
sealed trait RummageAppError extends NoStackTrace //We don't need the stacktrace, we know why it failed.

object RummageAppError {
  /**
   * There was an error with the data directory.
   * @param error [[zendesk.rummage.cli.model.DirectoryError]]
   */
  final case class AppDataDirectoryError(error: DirectoryError) extends RummageAppError

  /**
   * There was an error with loading the data.
   * @param error [[zendesk.rummage.cli.model.DataLoaderError]]
   */
  final case class AppDataLoaderError(error: DataLoaderError) extends RummageAppError
}
