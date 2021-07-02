package zendesk.rummage.cli.algebra

import zendesk.rummage.algebra.search.SearchEngineBuilder
import zendesk.rummage.cli.model.UserTicketDataBundle

import cats.effect.IO

trait RummageProgramBuilder[F[_]] {
  def build(dataBundle: UserTicketDataBundle): RummageProgram[F]
}

object RummageProgramBuilder {

  final class RummageProgramBuilderIO(consoleIO: Console[IO]) extends RummageProgramBuilder[IO] {

    override def build(dataBundle: UserTicketDataBundle): RummageProgram[IO] = {
      val quitProgram = new QuitMenuProgram.LiveQuitMenuProgram[IO]()

      val searchEngine =
        new SearchEngineBuilder.LiveSearchEngineBuilder(dataBundle.userData, dataBundle.ticketData).
          build()

      val userSearchResultsPrinter   = new UserSearchResultsPrinter.LiveUserSearchResultsPrinter()
      val ticketSearchResultsPrinter = new TicketSearchResultsPrinter.LiveTicketSearchResultsPrinter()
      val searchResultsPrinter       = new SearchResultsPrinter.SimpleSearchResultsPrinter(userSearchResultsPrinter, ticketSearchResultsPrinter)

      val searchMenuProgram    = new SearchMenuProgram.LiveSearchMenuProgram[IO](consoleIO, searchEngine, searchResultsPrinter)
      val dataSetMenuProgram   = new DataSetMenuProgram.LiveDataSetMenuProgram[IO](consoleIO, searchMenuProgram, quitProgram)
      val mainMenuProgram      = new MainMenuProgram.LiveMainMenuProgram[IO](consoleIO, dataSetMenuProgram, quitProgram)
      val program              = new RummageProgram.LiveRummageProgram[IO](consoleIO, mainMenuProgram)

      program
    }
  }
}
