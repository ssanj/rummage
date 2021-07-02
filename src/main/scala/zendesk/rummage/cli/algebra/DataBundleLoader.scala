package zendesk.rummage.cli.algebra

import zendesk.rummage.cli.model.DataLoaderError
import zendesk.rummage.cli.model.ValidDataDirectory
import zendesk.rummage.cli.model.RummageAppError

import io.circe.Decoder
import java.io.File

/**
 * Loads a "data bundle" from a list of files. A "data bundle" is a collection of decoded data read from various input files.
 * @tparam F The effect type
 */
trait DataBundleLoader[F[_]] {

  /**
   * Loads and decodes two data files and creates a data bundle from them.
   * @param dataDir The source directory for the input files
   * @param fileName1 The first input file
   * @param fileName2 The second input file
   * @param constructor Function that loads the content into a `DataBundle`
   * @tparam Input1 Type of the input data for file 1
   * @tparam Input2 Type of the input data for file 2
   * @tparam DataBundle The type that holds the data from the two input files after decoding
   * @return `F[DataBundle]` The data bundle in the effect type `F`
   */
  def loadDataSets[Input1: Decoder, Input2: Decoder, DataBundle](
    dataDir: File,
    fileName1: String,
    fileName2: String,
    constructor: (Vector[Input1], Vector[Input2]) => DataBundle): F[DataBundle]
}

object DataBundleLoader {
  import cats.MonadError
  import cats.Parallel
  import cats.data.EitherT
  import cats.implicits._

  private type DataBundleError[F[_]] = MonadError[F, Throwable]

  final class LiveDataBundleLoader[F[_]: Parallel: DataBundleError](
    dataSetLoader: DataSetLoader[F],
    directoryValidator: DirectoryValidator[F]) extends DataBundleLoader[F] {

    private def raiseError[A]: Throwable => F[A] = MonadError[F, Throwable].raiseError _

    override def loadDataSets[Input1: Decoder, Input2: Decoder, DataBundle](
      dataDir: File,
      fileName1: String,
      fileName2: String,
      constructor: (Vector[Input1], Vector[Input2]) => DataBundle): F[DataBundle] = {

        for {
          validatedDataDir <- validateDataDirectory(dataDir)
          dataBundle       <- loadDataSetsFrom[Input1, Input2, DataBundle](validatedDataDir, fileName1, fileName2, constructor)
        } yield dataBundle
    }

    private def validateDataDirectory(dataDir: File): F[ValidDataDirectory] = {
      EitherT(directoryValidator.validate(dataDir)).valueOrF[ValidDataDirectory] { e =>
        raiseError[ValidDataDirectory](RummageAppError.AppDataDirectoryError(e))
      }
    }

    @SuppressWarnings(Array("org.wartremover.warts.Any", "org.wartremover.warts.Nothing"))
    private def loadDataSetsFrom[Input1: Decoder, Input2: Decoder, DataBundle](
                                                                                dataDir: ValidDataDirectory,
                                                                                fileName1: String,
                                                                                fileName2: String,
                                                                                constructor: (Vector[Input1], Vector[Input2]) => DataBundle): F[DataBundle] = {
        val one = EitherT[F, DataLoaderError, Vector[Input1]](dataSetLoader.loadDataSet[Input1](dataDir, fileName1))
        val two = EitherT[F, DataLoaderError, Vector[Input2]](dataSetLoader.loadDataSet[Input2](dataDir, fileName2))

       val resultET = (one, two).parMapN(constructor)//load in parallel

       resultET.valueOrF(e => raiseError[DataBundle](RummageAppError.AppDataLoaderError(e)))
    }
  }
}
