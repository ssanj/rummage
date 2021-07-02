package zendesk.rummage.cli.algebra

import cats.effect.IO

/**
 * Handles console functionality
 * @tparam F Effect type
 */
trait Console[F[_]] {
  /**
   * Writes a message to the effect type `F`.
   * @param message The message to write
   * @return `F[Unit]` The effect of writing the message
   */
  def writeLn(message: String): F[Unit]

  /**
   * Reads a line of text from the effect type `F`.
   * @return `F[String]` The line read
   */
  def readLn(): F[String]
}

object Console {
  final class ConsoleIO() extends Console[IO] {

    override def writeLn(message: String): IO[Unit] = IO(println(message))

    override def readLn(): IO[String] = IO(scala.io.StdIn.readLine())
  }
}
