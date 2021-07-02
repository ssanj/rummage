package zendesk.rummage.cli.model

/**
 * Main menu commands
 */
sealed trait MainMenuCommand

object MainMenuCommand {
  /**
   * User chose the search option.
   */
  case object Search extends MainMenuCommand

  /**
   * User chose the quit option.
   */
  case object Quit extends MainMenuCommand
}
