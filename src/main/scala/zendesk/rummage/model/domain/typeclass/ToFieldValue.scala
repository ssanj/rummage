package zendesk.rummage.model.domain.typeclass

import zendesk.rummage.model.FieldValue

/**
 * Typeclass that describes how a type `T` is converted to [[zendesk.rummage.model.FieldValue]]s
 * @tparam T The type to convert
 */
trait ToFieldValue[T] { self =>
  /**
   * Returns the [[zendesk.rummage.model.FieldValue]] for an instance of `T`.
   * @param value The input type
   * @return [[zendesk.rummage.model.FieldValue]] for type `T`
   */
  def toFieldValue(value: T): FieldValue

  /**
   * Default [[zendesk.rummage.model.FieldValue]] when T has no data.
   */
  def defaultValue: FieldValue = FieldValue("")

  /**
   * If you can convert a type `S` to a type `T`, that has an instance of
   * [[zendesk.rummage.model.domain.typeclass.ToFieldValue]], then
   * you can get an instance of [[zendesk.rummage.model.domain.typeclass.ToFieldValue]] for `S`.
   *
   * @tparam S Type to convert
   * @param f Conversion function from a type `S` to a type `T`
   * @return [[zendesk.rummage.model.domain.typeclass.ToFieldValue]] for `S`
   */
  def contramap[S](f: S => T): ToFieldValue[S] = new ToFieldValue[S] {
    override def toFieldValue(value: S): FieldValue = self.toFieldValue(f(value))
    override def defaultValue: FieldValue = self.defaultValue //we use T's implementation for all things
  }
}

object ToFieldValue {

  /**
   * Manifests a given [[zendesk.rummage.model.domain.typeclass.ToFieldValue]] given `T` if one is in implicit scope.
   * @param fv The implicit [[zendesk.rummage.model.domain.typeclass.ToFieldValue]]
   * @tparam T The type to get the [[zendesk.rummage.model.domain.typeclass.ToFieldValue]] instance for
   * @return An instance of [[zendesk.rummage.model.domain.typeclass.ToFieldValue]]
   */
  def apply[T](implicit fv: ToFieldValue[T]): ToFieldValue[T] = fv

  implicit def optionToFieldValue[T: ToFieldValue]: ToFieldValue[Option[T]] = new ToFieldValue[Option[T]] {

    override def toFieldValue(value: Option[T]): FieldValue =
      value.fold(ToFieldValue[T].defaultValue)(ToFieldValue[T].toFieldValue)

    override  def defaultValue: FieldValue = ToFieldValue[T].defaultValue
  }

  implicit def seqToFieldValue[T: ToFieldValue]: ToFieldValue[Seq[T]] = new ToFieldValue[Seq[T]] {

    override def toFieldValue(value: Seq[T]): FieldValue =
      FieldValue(value.map(v => ToFieldValue[T].toFieldValue(v).value).mkString("[", ",", "]"))
  }

  implicit val stringToFieldValue: ToFieldValue[String] = new ToFieldValue[String] {
    override def toFieldValue(value: String): FieldValue = FieldValue(value)
  }

  implicit val longToFieldValue: ToFieldValue[Long] = new ToFieldValue[Long] {
    override def toFieldValue(value: Long): FieldValue = FieldValue(value.toString)
  }

  implicit val booleanToFieldValue: ToFieldValue[Boolean] = new ToFieldValue[Boolean] {

    override def toFieldValue(value: Boolean): FieldValue = FieldValue(value.toString)

  }
}
