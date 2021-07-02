package zendesk.rummage.model

import zendesk.rummage.model.domain.User
import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.field.Name
import zendesk.rummage.model.field.Timestamp
import zendesk.rummage.model.field.Verified
import zendesk.rummage.model.field.TicketId
import zendesk.rummage.model.field.TicketType
import zendesk.rummage.model.field.Subject
import zendesk.rummage.model.field.Tag

import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import java.time.OffsetDateTime
import java.time.ZoneOffset
import zendesk.rummage.model.domain.typeclass.ToFieldValue

//Generator for model types
object Generators {

  implicit val arbFieldName: Arbitrary[FieldName] = Arbitrary(Gen.alphaLowerStr.map(FieldName))

  implicit val arbFieldValue: Arbitrary[FieldValue] = Arbitrary(Gen.alphaLowerStr.map(ToFieldValue[String].toFieldValue))

  implicit val arbName: Arbitrary[Name] = Arbitrary(Gen.alphaLowerStr.map(Name.apply))

  implicit val arbTimestamp: Arbitrary[Timestamp] = Arbitrary {
    Gen.calendar.
      map(c => OffsetDateTime.ofInstant(c.toInstant(), ZoneOffset.UTC)).
      map(odt => Timestamp(odt.toString))
  }

  implicit val arbVerified: Arbitrary[Verified] = Arbitrary(Gen.prob(0.5).map(Verified.apply))

  implicit val arbUserId: Arbitrary[UserId] = Arbitrary(Gen.posNum[Long].map(UserId.apply))

  implicit val arbUser: Arbitrary[User] = Arbitrary {
    for {
      userId    <- Arbitrary.arbitrary[UserId]
      userName  <- Gen.option(Arbitrary.arbitrary[Name])
      createdAt <- Gen.option(Arbitrary.arbitrary[Timestamp])
      verified  <- Gen.option(Arbitrary.arbitrary[Verified])
    } yield User(userId, userName, createdAt, verified)
  }

  //Generate Users with unique (sequential) UserIds
  def genUsersWithUniqueIds(size: Int): Gen[List[User]] =  {
    def withId(uid: Int): Gen[User] = Arbitrary.arbitrary[User].map(u => u.copy(id = UserId(uid.toLong)))
    Gen.sequence[List[User], User]((1 to size).toList.map(withId))
  }

  implicit val arbTicket: Arbitrary[Ticket] = Arbitrary {
    for {
      id         <- Arbitrary.arbitrary[TicketId]
      createdAt  <- Gen.option(Arbitrary.arbitrary[Timestamp])
      ticketType <- Gen.option(Arbitrary.arbitrary[TicketType])
      subject    <- Gen.option(Arbitrary.arbitrary[Subject])
      assigneeId <- Gen.option(Arbitrary.arbitrary[UserId])
      tags       <- Gen.option(Arbitrary.arbitrary[Seq[Tag]])
    } yield Ticket(id, createdAt, ticketType, subject, assigneeId, tags)
  }

  implicit val arbTicketId: Arbitrary[TicketId] = Arbitrary(Gen.identifier.map(TicketId.apply))

  implicit val arbTicketType: Arbitrary[TicketType] = Arbitrary(Gen.alphaLowerStr.map(TicketType.apply))

  implicit val arbSubject: Arbitrary[Subject] = Arbitrary(Gen.alphaLowerStr.map(Subject.apply))

  implicit val arbTag: Arbitrary[Tag] = Arbitrary(Gen.alphaLowerStr.map(Tag.apply))
}
