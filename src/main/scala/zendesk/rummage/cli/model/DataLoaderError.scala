package zendesk.rummage.cli.model

/**
 * Types of errors that can occur when loading the data.
 */
sealed trait DataLoaderError

object DataLoaderError {
  /**
   * Could not decode the input.
   * @param error Circe error
   */
  final case class DecoderError(error: io.circe.Error) extends DataLoaderError

  /**
   * Could not load file.
   * @param error File loading error
   */
  final case class LoadingError(error: FileError) extends DataLoaderError

  def decoderError(error: io.circe.Error): DataLoaderError = DecoderError(error)
  def loadingError(error: FileError): DataLoaderError = LoadingError(error)
}
