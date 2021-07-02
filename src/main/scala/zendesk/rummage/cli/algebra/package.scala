package zendesk.rummage.cli

package object algebra {

  /**
   * Utility function to get the maximum Int value  of a `NonEmptyVector[Int]`.
   * @param values The collection of numbers to consider
   * @return The maximum value
   */
  def getMaxLength(values: cats.data.NonEmptyVector[Int]): Int = {
    values.tail.maxOption.fold(values.head)(Math.max(values.head, _))
  }
}
