package zendesk

package object rummage {

  /**
   * An inherently unsafe function to get a stacktrace from a Throwable.
   * This is probably ok as we are dumping out an exception.
   */
  def getStackTrace(throwable: Throwable): String = {
    import java.io.PrintWriter
    import java.io.StringWriter
    import scala.util.Try

    def getStackTraceTry(): Try[String] = Try {
      val out = new StringWriter()
      throwable.printStackTrace(new PrintWriter(out))
      out.toString()
    }

    getStackTraceTry().getOrElse("<could not retrieve stacktrace>")
  }

  /**
   * Get a stacktrace string from an optional Throwable.
   */
  def getStackTraceOp(throwableOp: Option[Throwable]): String = {
    throwableOp.fold("<no stacktrace>")(getStackTrace)
  }
}
