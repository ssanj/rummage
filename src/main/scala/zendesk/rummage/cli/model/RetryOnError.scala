package zendesk.rummage.cli.model

/**
 * Whether to retry on error.
 */
sealed trait RetryOnError
object RetryOnError {
  /**
   * Quit don't retry.
   */
  case object Quit extends RetryOnError

  /**
   * Retry.
   */
  case object Retry extends RetryOnError
}
