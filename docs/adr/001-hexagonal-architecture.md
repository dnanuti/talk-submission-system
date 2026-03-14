# ADR 001: Hexagonal Architecture

## Status
Accepted

## Context
We need a conference demo that teaches clean architecture under realistic constraints — multiple AI providers, multiple persistence options, and a domain model that enforces business rules. The demo must be small enough to read in one sitting but structured enough to demonstrate real trade-offs.

## Decision
Adopt hexagonal architecture (ports and adapters) with three layers: domain, application, and infrastructure. Dependencies point inward only.

## Consequences
- Domain model has zero framework dependencies — testable with plain JUnit
- Adding an AI provider or persistence implementation is a new adapter + one wiring change in AppConfig
- More files and indirection than a flat Spring MVC project (~40 files vs ~5)
- ArchUnit tests enforce the dependency rule in CI — accidental coupling is caught automatically
- Every port has at least two implementations, making the pattern concrete rather than theoretical
