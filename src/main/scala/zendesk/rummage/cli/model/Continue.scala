package zendesk.rummage.cli.model

/**
 * Specifies whether a user has chosen to exit the application or stay
 */
sealed trait Continuation

object Continuation {

  /**
   * User chose to stay
   */
  case object Stay extends Continuation

  /**
   * User chose to exit
   */
  case object Exit extends Continuation

  /**
   * @param continuation Continuation What the user chose (stay or exit)
   * @return true if the user chose to stay, false otherwise
   */
  def isStay(continuation: Continuation): Boolean = continuation match {
    case Stay => true
    case Exit => false
  }

  /**
   * @param continuation Continuation What the user chose (stay or exit)
   * @return true if the user chose to exit, false otherwise
   */
  def isExit(continuation: Continuation): Boolean = !isStay(continuation)
}
