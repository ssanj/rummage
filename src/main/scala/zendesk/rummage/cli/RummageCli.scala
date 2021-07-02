package zendesk.rummage.cli

import zendesk.rummage.cli.algebra.Console
import zendesk.rummage.cli.algebra.FileLoader
import zendesk.rummage.cli.algebra.DataSetLoader
import zendesk.rummage.cli.algebra.DataBundleLoader
import zendesk.rummage.cli.algebra.DirectoryValidator
import zendesk.rummage.cli.algebra.RummageProgramBuilder

import zendesk.rummage.model.domain.User
import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.cli.model.UserTicketDataBundle
import zendesk.rummage.cli.model.RummageAppError
import zendesk.rummage.cli.model.DataLoaderError
import zendesk.rummage.cli.model.FileError
import zendesk.rummage.cli.model.DirectoryError

import cats.effect.IO
import cats.effect.ExitCode

import java.io.File

final class RummageCli(dataDir: File, consoleIO: Console[IO]) {

  def run(): IO[ExitCode] = {
    val fileOpsIO = new FileLoader.FileLoaderIO()
    val dataSetLoaderIO: DataSetLoader[IO] = new DataSetLoader.LiveDataSetLoader[IO](consoleIO, fileOpsIO)
    val directoryValidatorIO = new DirectoryValidator.LiveDirectoryValidator[IO]()
    val dataSetOrchestratorIO = new DataBundleLoader.LiveDataBundleLoader[IO](dataSetLoaderIO, directoryValidatorIO)
    val programBuilderIO = new RummageProgramBuilder.RummageProgramBuilderIO(consoleIO)

    val programResultIO =
      for {
        dataBundle <- dataSetOrchestratorIO.loadDataSets[User, Ticket, UserTicketDataBundle](
                        dataDir,
                        "users.json",
                        "tickets.json",
                        UserTicketDataBundle
                      )
        program = programBuilderIO.build(dataBundle)
        _ <- program.run()
      } yield ()

    programResultIO.attempt.flatMap {
      case Left(e: RummageAppError) =>
        val logError =
          e match {
             case RummageAppError.AppDataDirectoryError(DirectoryError.NotADirectory(dir)) =>
              consoleIO.writeLn(s"Data directory: ${dir.toString} is not a valid directory")

             case RummageAppError.AppDataDirectoryError(DirectoryError.NotReadable(dir)) =>
              consoleIO.writeLn(s"Data directory: ${dir.toString} is not readable")

             case RummageAppError.AppDataLoaderError(DataLoaderError.DecoderError(circeError)) =>
              consoleIO.writeLn(s"Error decoding data: ${circeError.getMessage}")

             case RummageAppError.AppDataLoaderError(DataLoaderError.LoadingError(FileError.CouldNotLoadFile(file, reason, trace))) =>
              val stackTrace = zendesk.rummage.getStackTraceOp(trace)
              consoleIO.writeLn(s"Error loading file: $file due to: $reason, \ntrace:\n  $stackTrace")
          }

          logError.as(ExitCode.Error)

      case Left(error) =>
        val stackTrace = zendesk.rummage.getStackTrace(error)
        consoleIO.writeLn(s"Rummage failed with an error: ${error.getMessage}, \ntrace:\n  $stackTrace").as(ExitCode.Error)
      case Right(_)    => IO.pure(ExitCode.Success)
    }
  }
}
