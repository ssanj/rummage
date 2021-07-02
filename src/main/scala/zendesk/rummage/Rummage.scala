package zendesk.rummage

import zendesk.rummage.cli.algebra.Console
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.ExitCode

import java.io.File


object Rummage extends IOApp {
  private val consoleIO: Console[IO] = new Console.ConsoleIO()

  def run(args: List[String]): IO[ExitCode] = args match {
    case dataDir :: _ => new cli.RummageCli(new File(dataDir), consoleIO).run()
    case Nil          => consoleIO.writeLn("You must supplied a data directory as the first argument").as(ExitCode.Error)
  }


}
