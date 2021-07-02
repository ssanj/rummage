package zendesk.rummage.cli.algebra

trait RummageProgram[F[_]] {
  def welcomeMessage: List[String]
  def run(): F[Unit]
}

object RummageProgram {

  import cats.Monad
  import cats.implicits._

  final class LiveRummageProgram[F[_]: Monad](console: Console[F], mainMenuProgram: MainMenuProgram[F]) extends RummageProgram[F] {

    override def welcomeMessage: List[String] =
      List(
        "",
        "",
        "Welcome to Rummage",
        "==================",
        ""
      )

    override def run(): F[Unit] = {
      for {
        _ <- console.writeLn(welcomeMessage.mkString("\n"))
        _ <- mainMenuProgram.run()
      } yield ()
    }
  }
}
