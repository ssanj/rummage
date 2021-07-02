package zendesk.rummage.cli.model

import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.model.domain.User

/**
 * Data bundle that contains a collection of [[zendesk.rummage.model.domain.User]] and [[zendesk.rummage.model.domain.Ticket]].
 * @param userData The user data
 * @param ticketData The ticket data
 */
final case class UserTicketDataBundle(userData: Vector[User], ticketData: Vector[Ticket])
