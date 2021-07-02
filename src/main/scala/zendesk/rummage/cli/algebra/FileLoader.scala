package zendesk.rummage.cli.algebra

import cats.effect.kernel.Resource
import zendesk.rummage.cli.model.FileError

import java.io.File
import scala.io.{BufferedSource, Codec, Source}
import scala.util.Try

/**
 * Handles loading of files.
 * @tparam F The effect type
 */
trait FileLoader[F[_]] {
  /**
   * Loads a given file from a given path. The output is chosen as String because we only load
   * JSON files.
   * @param parentDir The path to load the data file from.
   * @param fileName The data file name in the given path.
   * @return Either a [[zendesk.rummage.cli.model.FileError]] on the left if there is an error or a String with the data
   * on the right in an effect `F`.
   */
  def loadAbsoluteFile(parentDir: File, fileName: String): F[Either[FileError, String]]
}

object FileLoader {

  import cats.effect.IO

  final class FileLoaderIO() extends FileLoader[IO] {

    override def loadAbsoluteFile(parentDir: File, fileName: String): IO[Either[FileError, String]] = {
      val contentIO = for {
        file        <- IO(new File(parentDir, fileName))
        fileContent <- getFileContentR(file).use(bs => IO(bs.mkString))
      } yield fileContent

      contentIO.attempt.map {
        case Left(error)    =>
          Left[FileError, String](FileError.CouldNotLoadFile(getFilePath(parentDir, fileName), error.getMessage, Some(error)))
        case Right(content) => Right[FileError, String](content)
      }
    }

    private def getFileContentR(inputFile: File): Resource[IO, BufferedSource] =
      Resource.make[IO, BufferedSource](IO(Source.fromFile(inputFile)(Codec.UTF8)))(bs => IO(bs.close()))
  }

  def getFilePath(parentDir: File, fileName: String): String = {
    val optName =
      for {
        parentPath <-  Try(parentDir.getPath()).toOption.orElse(Some("<unknown>"))
        file       <-  Option(fileName).orElse(Some("<unknown>"))
      } yield s"${parentPath}/$file"

    optName.getOrElse("Could not decipher file name")
  }
}
