package zendesk.rummage.model.domain

import zendesk.rummage.model.field.UserId
import zendesk.rummage.model.field.Name
import zendesk.rummage.model.field.Verified
import zendesk.rummage.model.field.Timestamp
import zendesk.rummage.model.field.TicketId
import zendesk.rummage.model.field.TicketType
import zendesk.rummage.model.field.Subject
import zendesk.rummage.model.field.Tag

import org.scalatest.Assertion
import org.scalatest.Assertions.fail
import org.scalatest.Assertions.assert
import io.circe.Error

object DomainHelper {

  def createUser(id: Long, name: String, createdAt: String, verified: Boolean): User = {
    User(UserId(id), Some(Name(name)), Some(Timestamp(createdAt)), Some(Verified(verified)))
  }

  def createSimpleUser(id: Long): User = {
    val name, createdAt, verified = None
    User(UserId(id), name, createdAt, verified)
  }

  def createTicket(id: String, createdAt: String, ticketType: String, subject: String, assigneeId: Long, tags: Seq[String]): Ticket = {
    Ticket(
      TicketId(id),
      Some(Timestamp(createdAt)),
      Some(TicketType(ticketType)),
      Some(Subject(subject)),
      Some(UserId(assigneeId)),
      Some(tags.map(Tag.apply))
    )
  }

  def createSimpleTicket(id: String): Ticket = {
    val createdAt, ticketType, subject, assigneeId, tags = None
    Ticket(
      TicketId(id),
      createdAt,
      ticketType,
      subject,
      assigneeId,
      tags
    )
  }

  def assertDecoder[A](decodeResult: Either[Error, A], expected: A): Assertion =
    decodeResult.fold(failWithError, assertDomain(expected))

  def assertDecoderError[A](decodeResult: Either[Error, A], expectedError: Error => Assertion): Assertion = {
    decodeResult.fold(expectedError, r => fail(s"Expected a decoder error but got: $r"))
  }

  def assertDomain[A](expected: A)(actual: A): Assertion = assert(expected == actual)

  def failWithError(e: Error): Assertion = {
    fail(e.getMessage())
  }



}
