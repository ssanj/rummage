package zendesk.rummage.cli.algebra

import zendesk.rummage.cli.model.DataLoaderError
import zendesk.rummage.cli.model.ValidDataDirectory

import io.circe.Decoder
import io.circe.parser.decode

/**
 * Loads a data file from a given input directory and decodes it to a given domain type.
 * @tparam F The effect type
 */
trait DataSetLoader[F[_]] {
  /**
   * Loads a specific data file from the given data directory decoding it into a domain type `A`.
   * @param dataDir The data directory that contains the named file to import
   * @param fileName The name of the file whose contents needs to be returned
   * @tparam A The data type that is used to decode the input data
   * @return Vector[A] All the input data decoded into values of type `A`
   */
  def loadDataSet[A: Decoder](dataDir: ValidDataDirectory, fileName: String): F[Either[DataLoaderError, Vector[A]]]
}


object DataSetLoader {
  import cats.Monad
  import cats.implicits._
  final class LiveDataSetLoader[F[_]: Monad](console: Console[F], fileLoader: FileLoader[F]) extends DataSetLoader[F] {
    def loadDataSet[A: Decoder](dataDir: ValidDataDirectory, fileName: String): F[Either[DataLoaderError, Vector[A]]] = {
      for {
        _ <- console.writeLn(s"Loading file: $fileName")
        contentE <- fileLoader.loadAbsoluteFile(dataDir.dataDirectory, fileName).map(_.leftMap(DataLoaderError.loadingError))
        _ <- console.writeLn(s"Loaded file: $fileName")
        decodedContentE = decodeContent[A](contentE)
        _ <- console.writeLn(s"Decoded file: $fileName")
      } yield decodedContentE
    }

    private def decodeContent[A: Decoder](contentE: Either[DataLoaderError, String]): Either[DataLoaderError, Vector[A]] = for {
      content        <- contentE
      decodedContent <- decode[Vector[A]](content).leftMap(DataLoaderError.decoderError)
    } yield decodedContent
  }
}
