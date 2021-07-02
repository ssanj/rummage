package zendesk.rummage.algebra.search

import zendesk.rummage.model.domain.User
import zendesk.rummage.model.field.UserId

import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop.propBoolean
import org.scalacheck.Gen
import Generators._
import zendesk.rummage.model.domain.DataMapper

object DefaultDataSetProcessorProps extends Properties("UserData") {

  //The number of keys in the processed data set map should be equal to the number of elements in the data set
  property("processDataSet.keys") = {
    val dataSize = 100
    Prop.forAll(genUserData(dataSize)){ userData =>
      val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](userData)
      val userIdToUserMap = dataSetProcessor.processDataSet()
      val actualKeySize = userIdToUserMap.keys.size

      (actualKeySize == dataSize) :| s"Expected $dataSize keys but got: $actualKeySize\n$userData}"
    }
  }

  //Choosing any random index from a data set should return a User record with that UserId
  property("processDataSet.value") = {
    val dataSize = 100
    Prop.forAll(Gen.chooseNum(1, 100).map((UserId.apply _) compose (_.toLong)), genUserData(dataSize)){ (randomKey, userData) =>
      val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](userData)
      val userIdToUserMap = dataSetProcessor.processDataSet()

      val userValue = userIdToUserMap.get(randomKey)

      userValue match {
        case Some(user) => (user.id == randomKey) :|
          s"Expected user to have id: $randomKey but got: ${user.id}"

        case None => Prop.falsified :| s"Expected to find matching user for id: $randomKey\n${userData.mkString("\n")}"
      }
    }
  }

  //The number of fields in the field index should be the same as the number of fields
  property("processFieldIndex.fieldNameKeys") = {
    val dataize = 100
    Prop.forAll(genUserData(dataize)){ userData =>
      val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](userData)
      val fieldValuesMap = dataSetProcessor.processFieldIndex()

      val actualKeySize = fieldValuesMap.keys.size
      val expectedKeySize = DataMapper[UserId, User].fieldMappings.length
      (actualKeySize == expectedKeySize) :| s"Expected $expectedKeySize keys but got: $actualKeySize\n${userData.mkString("\n")}}"
    }
  }

  //Choosing a random FieldName and FieldValue with a know UserId should, should match up when looking up
  //FieldName and FieldValue in the Map returned by DataSetProcessor.processFieldIndex.
  property("processFieldIndex.value") = {
    val dataSetSize = 1000 //bump the size so there is more opportunity for something to go wrong
    Prop.forAll(genUserDataWithFieldNameValue(dataSetSize)){
      case SearchResultExpectation(userData, userId, fieldName, fieldValue) =>
        val dataSetProcessor = new DataSetProcessor.DefaultDataSetProcessor[UserId, User](userData)
        val fieldValuesMap = dataSetProcessor.processFieldIndex()

        val actualUserIds = for {
         fieldValues <- fieldValuesMap.get(fieldName)
         userIds     = fieldValues(fieldValue)
        } yield userIds

        actualUserIds match {
          case Some(userIds) => (userIds.contains(userId)) :|
            s"Could not find user id: $userId in: ${userIds.mkString(",")}"

          case None => Prop.falsified :| s"Expected to find matching user for id: $userId\n${userData.mkString("\n")}"
        }
    }
  }
}
