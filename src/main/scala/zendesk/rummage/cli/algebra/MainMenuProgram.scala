package zendesk.rummage.cli.algebra

import zendesk.rummage.cli.model.Continuation
import zendesk.rummage.cli.model.MainMenuCommand

trait MainMenuProgram[F[_]] {
  def mainMenu: List[String]
  def toCommand(command: String): Either[String, MainMenuCommand]
  def run(): F[Continuation]
  def handleCommandError(commandError: String): F[Continuation]
  def runAgainDescription: List[String]
  def runAgain(): F[Continuation]
  def toRunAgainCommand(command: String): Either[MainMenuCommand.Quit.type, MainMenuCommand.Search.type]
}

object MainMenuProgram {
  import cats.Monad
  import cats.implicits._

  final class LiveMainMenuProgram[F[_]: Monad](console: Console[F], dataSetMenuProgram: DataSetMenuProgram[F], quitMenuProgram: QuitMenuProgram[F]) extends MainMenuProgram[F] {

    override def mainMenu: List[String] =
      List(
        "  Commands you can run:",
        "  - Type ':s' to search across the available data sets",
        "  - Type ':q' to quit",
        "",
        "Please enter a command:"
      )

    override def run(): F[Continuation] = {
      for {
        _ <- console.writeLn(mainMenu.mkString("\n"))
        command <- console.readLn()
        commandE = toCommand(command)
        result <- commandE match {
          case Right(MainMenuCommand.Search) => handleSearch()
          case Right(MainMenuCommand.Quit)   => quitMenuProgram.run()
          case Left(commandError)            => handleCommandError(commandError)
        }
      } yield result
    }

    def handleSearch(): F[Continuation] = {
      for {
       cont           <- dataSetMenuProgram.run()
       continueResult <- if (Continuation.isStay(cont)) runAgain() else quitMenuProgram.run()
      } yield continueResult
    }

    override def toCommand(command: String): Either[String, MainMenuCommand] = command match {
      case ":s"  => Right[String, MainMenuCommand](MainMenuCommand.Search)
      case ":q"  => Right[String, MainMenuCommand](MainMenuCommand.Quit)
      case other => Left[String, MainMenuCommand](other)
    }

    override def toRunAgainCommand(command: String): Either[MainMenuCommand.Quit.type, MainMenuCommand.Search.type] = command match {
      case ":q" => Left[MainMenuCommand.Quit.type, MainMenuCommand.Search.type](MainMenuCommand.Quit)
      case _    => Right[MainMenuCommand.Quit.type, MainMenuCommand.Search.type](MainMenuCommand.Search)
    }


    override def handleCommandError(commandError: String): F[Continuation] = {
      console.writeLn(s"I don't know what you mean. `$commandError` is not a valid command") *>
      console.writeLn("") *>
      runAgain()
    }

    override def runAgainDescription: List[String] = List("Please ':q' to quit or enter to search again")

    override def runAgain(): F[Continuation] = for {
      _ <- console.writeLn(runAgainDescription.mkString("\n"))
      command <- console.readLn()
      commandE  = toRunAgainCommand(command)
      result <- commandE match {
        case Left(MainMenuCommand.Quit)    => quitMenuProgram.run()
        case Right(MainMenuCommand.Search) => handleSearch()
      }
    } yield result
  }
}
