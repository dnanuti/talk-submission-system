# ADR 005: Expressive Domain Methods over Setters

## Status
Accepted

## Context
A talk submission follows a strict lifecycle: Draft → Submitted → Under Review → Accepted / Rejected. This lifecycle has guards (you can't accept a draft) and side effects (domain events). Using setters (`setStatus(ACCEPTED)`) would bypass both.

## Decision
Expose intention-revealing methods on the `Talk` aggregate: `submitForReview()`, `addReview()`, `accept()`, `reject(reason)`. No public setters. No public constructor for regular use — only the `Talk.draft()` factory and a package-level constructor for persistence reconstitution.

Each method:
1. Guards against invalid state transitions
2. Updates the state
3. Raises a domain event

## Consequences
- Invalid transitions are impossible at the API level — compile-time or runtime enforcement
- The method names match the ubiquitous language ("submit for review", "reject with reason")
- Tests read like specifications: `talk.accept(now)` instead of `talk.setStatus(ACCEPTED)`
- Trade-off: the persistence layer needs a way to reconstitute from stored state, which requires a full constructor — but it's package-private, not part of the public domain API
