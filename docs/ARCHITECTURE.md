# Architecture

## The hexagonal model

This project follows hexagonal architecture (ports and adapters). The core idea: business logic lives in the centre, isolated from all external concerns — databases, HTTP, AI providers, message queues. The outside world connects through ports (interfaces) and adapters (implementations).

```
                    ┌─────────────────────────────┐
    Driving         │        Application          │         Driven
    adapters        │                             │         adapters
                    │    ┌───────────────┐        │
 ┌──────────┐       │    │               │        │       ┌───────────┐
 │   REST   │──in──▶│    │    Domain     │──data──┼──▶    │    JPA    │
 └──────────┘       │    │     Model     │  port  │       └───────────┘
 ┌──────────┐       │    │               │        │       ┌───────────┐
 │  Mobile  │──in──▶│    │  Talk.java    │──AI────┼──▶    │  OpenAI   │
 └──────────┘       │    │               │  port  │       ├───────────┤
                    │    └───────────────┘        │       │  Gemini   │
                    │                             │       ├───────────┤
                    └─────────────────────────────┘       │  Local    │
                                                          │  Fallback │
                                                          └───────────┘
```

## Dependency direction

Dependencies point inward. Always.

```
infrastructure → application → domain
     never ←         never ←
```

- **Domain** (`domain.model`, `domain.event`): Pure Java. No imports from Spring, JPA, infrastructure, or application. This is enforced by ArchUnit tests.
- **Application** (`application.port`, `application.service`): Orchestrates domain objects through ports. Depends on domain, never on infrastructure.
- **Infrastructure** (`infrastructure.adapters`): Implements ports. Depends on application and domain. This is the only layer that knows about Spring, JPA, HTTP, and AI SDKs.

The `AppConfig` class wires adapters to ports — it's the composition root. When you want to swap an implementation, you change one line in `AppConfig`, not the domain or application layer.

## Ports

### Driving ports (inbound)

These define what the application **can do** — the use cases it supports.

| Port | Purpose |
|---|---|
| `SubmitTalkUseCase` | Create drafts, submit for review |
| `ReviewTalkUseCase` | Add manual reviews, accept, reject |
| `GetTalkQuery` | Read talks (CQRS-style separation) |
| `TestSupportUseCase` | Reset state for testing |

### Driven ports (outbound)

These define what the application **needs** — external capabilities it depends on.

| Port | Purpose |
|---|---|
| `TalkDataPort` | Persist and retrieve talks |
| `AIPort` | Evaluate a talk submission |

## AI as a port

The AI evaluation is behind `AIPort`, which takes a `TalkEvaluationRequest` — not the full `Talk` aggregate. This is a deliberate boundary:

1. **The AI provider doesn't see domain internals.** It gets `title`, `abstractText`, `speakerName` — just what it needs to evaluate. It doesn't know about status, reviews, or domain events.
2. **Swapping providers is a wiring change.** OpenAI, Gemini, local rules — all implement the same `AIPort` interface. The domain doesn't know which one ran.
3. **Failure is expected.** AI providers go down, time out, return garbage. The `ResilientAIAdapter` chains multiple providers and falls back automatically. Business logic is untouched.

### The resilience chain

```java
new ResilientAIAdapter(List.of(openAI, gemini, localFallback))
```

If OpenAI fails → try Gemini. If Gemini fails → use local heuristic rules. If all fail → throw with suppressed exceptions so you can see what went wrong in each provider.

This is the composite pattern applied to resilience. Each adapter is independently testable. The chain itself is testable. No retry libraries, no circuit breakers — just a list and a for-loop.

## Domain model

`Talk` is the aggregate root. It enforces its own state transitions:

```
DRAFT ──submitForReview()──▶ SUBMITTED ──addReview()──▶ UNDER_REVIEW
                                                            ├── accept() ──▶ ACCEPTED
                                                            └── reject() ──▶ REJECTED
```

Every state transition:
- Guards against invalid transitions (e.g., you can't accept a draft)
- Raises a domain event (e.g., `TalkSubmitted`, `ReviewAdded`)
- Is testable without any framework

The aggregate collects domain events internally. The application service clears them after processing. This keeps the domain pure while still enabling event-driven patterns.

## Package structure rationale

The package structure mirrors the hexagonal layers:

- `domain.model` — aggregates, value objects, enums
- `domain.event` — domain events (records)
- `application.port.in` — driving port interfaces and command objects
- `application.port.out` — driven port interfaces
- `application.service` — use case implementations
- `infrastructure.adapters.in.*` — driving adapters (web, mobile, test support)
- `infrastructure.adapters.out.*` — driven adapters (AI, persistence)

This makes the dependency rule visible in the import statements. If you see `import ...infrastructure...` inside `application` or `domain`, something is wrong — and ArchUnit will catch it.

## When NOT to use hexagonal architecture

Hexagonal architecture adds indirection. That indirection pays for itself when:
- You have multiple implementations of the same port (3 AI providers, 3 persistence options)
- You need to test business logic without infrastructure
- You expect external dependencies to change (AI providers, database vendors)
- Multiple teams work on different adapters

It's **not worth it** when:
- **The project is a CRUD wrapper.** If your "domain logic" is just save-and-retrieve, the ports and adapters are ceremony for ceremony's sake.
- **You have one database and one API.** If there's exactly one implementation of every port and no realistic chance of swapping, you're writing interfaces nobody will implement.
- **The team is small and the project is short-lived.** Architecture is a bet on the future. A hackathon prototype doesn't need it.
- **You're building a script or CLI tool.** Hexagonal architecture is for systems with multiple integration points. A batch job that reads a file and writes to a database probably doesn't need ports.

The honest test: if you remove the ports and have adapters call domain objects directly, does anything actually get harder? If not, you don't need the indirection yet.
