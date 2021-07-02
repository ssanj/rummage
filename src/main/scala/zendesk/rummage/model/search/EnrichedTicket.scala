package zendesk.rummage.model.search

import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.model.domain.User

/**
 * Ticket with associated assignee information.
 */
final case class EnrichedTicket(ticket: Ticket, assignee: Option[User])
