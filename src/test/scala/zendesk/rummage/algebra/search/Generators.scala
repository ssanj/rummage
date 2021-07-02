package zendesk.rummage.algebra.search


import zendesk.rummage.model.FieldName
import zendesk.rummage.model.FieldValue

import zendesk.rummage.model.domain.User
import zendesk.rummage.model.field.UserId

import org.scalacheck.Gen
import zendesk.rummage.model.Generators._
import zendesk.rummage.model.domain.DataMapper

//Generator for search types
object Generators extends {

  def genUserData(size: Int): Gen[Vector[User]] =
    genUsersWithUniqueIds(size).map(_.toVector)

  /**
   * Contains data and path to search through that using a [[zendesk.rummage.model.FieldName]],
   * [[zendesk.rummage.model.FieldValue]]. The result should include the supplied id.
   *
   * @param data [[scala.collection.immutable.Vector[Domain]]]
   * @param id the PK of the to partition the data by
   * @param fieldName [[zendesk.rummage.model.FieldName]]
   * @param fieldValue [[zendesk.rummage.model.FieldValue]]
   * @tparam PK The primary key type of the data
   * @tparam Domain The domain type of the data
   */
  final case class SearchResultExpectation[PK, Domain](data: Vector[Domain], id: PK, fieldName: FieldName, fieldValue: FieldValue)

  def genUserDataWithFieldNameValue(size: Int): Gen[SearchResultExpectation[UserId, User]] = {
    for {
      userIndex <- Gen.chooseNum(0, size - 1) //user record to choose
      data      <- genUsersWithUniqueIds(size).map(_.toVector) //UserDataSet of size: size

      fieldMappings = DataMapper[UserId, User].fieldMappings //field mappings of type DataSetFieldMapping
      randomFieldNameIndex <- Gen.chooseNum(0, fieldMappings.length - 1) //choose a random index into the field mapping
      fieldMapping = fieldMappings.toVector(randomFieldNameIndex) //choose a random field mapping

      fieldName = fieldMapping.domainField //grab the domain field name FieldName
      userRow = data(userIndex) //select the user, identified by userIndex
      fieldValue = fieldMapping.getter(userRow)//grab the value of the chosen User; userRow
    } yield SearchResultExpectation(data, userRow.id, fieldName ,fieldValue)

  }

}
