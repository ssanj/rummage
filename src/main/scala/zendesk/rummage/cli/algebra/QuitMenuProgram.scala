package zendesk.rummage.cli.algebra

import zendesk.rummage.cli.model.Continuation

trait QuitMenuProgram[F[_]] {
  def run(): F[Continuation]
}

object QuitMenuProgram {
  import cats.Applicative

  final class LiveQuitMenuProgram[F[_]: Applicative]() extends QuitMenuProgram[F] {
    def run(): F[Continuation] = Applicative[F].pure(Continuation.Exit)
  }
}
