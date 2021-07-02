package zendesk.rummage.model.search

import zendesk.rummage.model.domain.Ticket
import zendesk.rummage.model.domain.User

/**
 * User with associated tickets.
 */
final case class EnrichedUser(user: User, tickets: Vector[Ticket])
