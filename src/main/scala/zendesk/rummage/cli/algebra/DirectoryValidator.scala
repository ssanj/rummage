package zendesk.rummage.cli.algebra

import java.io.File
import zendesk.rummage.cli.model.ValidDataDirectory
import zendesk.rummage.cli.model.DirectoryError
import cats.Parallel
import cats.effect.Sync

/**
 * Validates a directory on some criterion.
 * @tparam F The effect type
 */
trait DirectoryValidator[F[_]] {

  /**
   * Validates a directory supplied based on whether it is:
   * - A directory
   * - Readable
   * @param dir The directory to validate
   * @return An `F[Right[ValidDataDirectory]]` if the directory is valid or a `F[Left[DirectoryError]]`` if it is not
   */
  def validate(dir: File): F[Either[DirectoryError, ValidDataDirectory]]
}

object DirectoryValidator {

  import cats.implicits._
  final class LiveDirectoryValidator[F[_]: Parallel : Sync] extends DirectoryValidator[F] {
    def validate(dir: File): F[Either[DirectoryError, ValidDataDirectory]] = {
        (Sync[F].delay(dir.isDirectory()), Sync[F].delay(dir.canRead())).parMapN {
          case (true, true)   => Right[DirectoryError, ValidDataDirectory](ValidDataDirectory(dir))
          case (true, false)  => Left[DirectoryError, ValidDataDirectory](DirectoryError.NotReadable(dir))
          //In either case the dir is not a directory, so return that error
          case (false, _) => Left[DirectoryError, ValidDataDirectory](DirectoryError.NotADirectory(dir))
        }
    }
  }
}
