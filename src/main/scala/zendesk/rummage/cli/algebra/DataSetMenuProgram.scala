package zendesk.rummage.cli.algebra

import zendesk.rummage.cli.model.Continuation
import zendesk.rummage.cli.model.RetryOnError
import zendesk.rummage.model.TableType

import scala.util.Try

trait DataSetMenuProgram[F[_]] {
  def dataSetMenu: List[String]
  def getTableType(indexStr: String): Option[TableType]
  def run(): F[Continuation]
  def handleInvalidDataSetSelection(): F[RetryOnError]
  def toRetryDataSetSelection(command: String): RetryOnError
}


object DataSetMenuProgram {
  import cats.Monad

  final class LiveDataSetMenuProgram[F[_]: Monad](console: Console[F], searchMenuProgram: SearchMenuProgram[F], quitMenuProgram: QuitMenuProgram[F]) extends DataSetMenuProgram[F] {
    import cats.implicits._

    private def getTableIndexStrings: List[String] = {
      tableTypeKeys.map { case (index, tableType) => s" - [${index.toString}] ${TableType.simpleString(tableType)}"}.toList
    }

    private def getSearchLegend: String = {
      val legends = tableTypeKeys.map { case (index, tableType) =>  s"${index.toString} to search ${TableType.simpleString(tableType)}"}
      legends.mkString("Please enter ", ", ", ".")
    }

    override def dataSetMenu: List[String] =
       ("We have the following data sets:" +: getTableIndexStrings) ++ List("", getSearchLegend)

    private val tableTypes: List[TableType] = List[TableType](TableType.UserTableType, TableType.TicketTableType)

    private val tableTypeKeys: Map[Int, TableType] =
      tableTypes.zipWithIndex.map { case (tableType, index) => (index + 1, tableType) }.toMap

    override def getTableType(indexStr: String): Option[TableType] =
      for {
        index <- Try(indexStr.toInt).toOption
        result <- tableTypeKeys.get(index)
      } yield result

    override def run(): F[Continuation] = {
      for {
        _ <- console.writeLn(dataSetMenu.mkString("\n"))
        datasetChoice <- console.readLn()
        tableTypeOp = getTableType(datasetChoice)
        result <- tableTypeOp match {
          case Some(tableType) => searchMenuProgram.run(tableType)
          case None            => handleInvalidDataSetSelection() >>= retryProgramOrQuit
        }
      } yield result
    }

    override def handleInvalidDataSetSelection(): F[RetryOnError] = {
      for {
        _ <- console.writeLn("Invalid data set choice")
        _ <- console.writeLn("Please enter ':q' to quit and enter to try again")
        quitOrRetry <- console.readLn()
        response = toRetryDataSetSelection(quitOrRetry)
      } yield response
    }

    private def retryProgramOrQuit(retryOption: RetryOnError): F[Continuation] = {
      retryOption match {
        case RetryOnError.Retry => run()
        case RetryOnError.Quit  => quitMenuProgram.run()
      }
    }

    override def toRetryDataSetSelection(command: String): RetryOnError = command match {
      case ":q" => RetryOnError.Quit
      case _    => RetryOnError.Retry
    }
  }
}
