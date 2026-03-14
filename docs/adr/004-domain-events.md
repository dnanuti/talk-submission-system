# ADR 004: Domain Events on the Aggregate

## Status
Accepted

## Context
State transitions in the domain model (submitted, reviewed, accepted, rejected) are meaningful business moments. Other parts of the system might want to react — send notifications, update dashboards, audit the decision trail.

## Decision
The `Talk` aggregate collects domain events internally. Each state-changing method appends an event record (`TalkSubmitted`, `ReviewAdded`, `TalkAccepted`, `TalkRejected`). The application service reads them after save and clears them.

Events are records implementing a `DomainEvent` interface with `occurredAt()` and `talkId()`.

## Alternatives considered
- **Spring ApplicationEvent**: Couples the domain to Spring. We explicitly want domain to have zero Spring dependencies.
- **Event sourcing**: Full event sourcing is powerful but far beyond what this demo needs. We store current state, not event streams.
- **No events at all**: Simpler, but misses a key DDD teaching point and makes side-effect extension harder.

## Consequences
- Domain events are raised inside the aggregate — they're always consistent with the state transition
- No event bus or publisher in the domain layer — that's an infrastructure concern
- Events are ephemeral (cleared after processing) — not persisted as an event store
- Adding a side-effect (email on acceptance, Slack on rejection) means adding an event handler, not modifying the aggregate
