# Talk Submission System

**DUBJug — Designing for Change**
This is the **hexagonal architecture** branch. Compare it with the ["Big Ball of Mud" version](../../tree/dub-jug/example/muddy) to see the same features implemented without architectural separation.

A teaching-oriented Java project demonstrating **hexagonal architecture**, **domain-driven design**, and **AI resilience** — built as a companion to the "Designing for Change" conference talk.

The system lets conference organisers manage talk submissions through a lifecycle: **Draft → Submitted → Under Review → Accepted / Rejected**. An AI port evaluates submissions, with a resilient failover chain that gracefully degrades when providers fail.

## Why this project exists

Architecture patterns are easier to understand through running code than slides. This project is intentionally small (~40 source files) so you can read the whole thing in one sitting — but it demonstrates the same structural decisions you'd make in a production system.

## Quick start

```bash
mvn spring-boot:run
```

Then try the example flow:

```bash
# Create a draft
curl -s -X POST http://localhost:8080/api/talks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hexagonal Architecture for Real Teams",
       "abstractText":"This session shows how ports and adapters help teams contain change, add AI resilience, and keep business logic clean in Spring Boot systems.",
       "speakerName":"Diana"}' | jq

# Submit for AI review
curl -s -X POST http://localhost:8080/api/talks/1/submit | jq

# Accept after review
curl -s -X POST http://localhost:8080/api/talks/1/accept | jq

# Or reject with a reason
curl -s -X POST http://localhost:8080/api/talks/1/reject \
  -H 'Content-Type: application/json' \
  -d '{"reason":"Topic already covered this year"}' | jq
```

### Trigger AI failure modes

Include magic strings in the abstract to simulate failures:

| Abstract contains     | What happens                                    |
|-----------------------|-------------------------------------------------|
| `fail-openai`         | OpenAI throws → Gemini handles it               |
| `fail-gemini`         | Gemini throws → next in chain handles it         |
| `timeout-openai`      | OpenAI simulates a 30s timeout                   |
| `malformed-gemini`    | Gemini returns a malformed response              |
| Both `fail-openai` + `fail-gemini` | Both remote providers fail → local rules fallback |

## Learning paths by audience level

### Junior developer
Start here to understand why architecture matters beyond "it compiles."

1. **Read `Talk.java`** — the domain model. Notice how it enforces its own rules (you can't accept a draft, you can't review after a decision). No framework magic, just Java.
2. **Run `TalkTest.java`** — every business rule has a test. These tests don't need Spring, a database, or HTTP. They run in milliseconds.
3. **Try the curl commands** above and watch the status transitions.
4. **Read `AIFailureModeTest.java`** — see what happens when AI providers fail, and how the fallback chain catches it.

### Senior engineer
You already know the theory. Focus on the trade-offs.

1. **Read `docs/ARCHITECTURE.md`** — dependency direction, port design, why `TalkEvaluationRequest` exists.
2. **Read the ADRs** in `docs/adr/` — each one captures a decision you'd face in production.
3. **Compare `ResilientAIAdapter`** with `OpenAIAdapter` — the composite pattern gives you resilience without touching business logic.
4. **Read `HexagonalArchitectureTest.java`** — ArchUnit enforces that domain stays free of Spring/JPA/infrastructure dependencies. This is how you keep the architecture honest.

### Tech lead / Staff+
You're evaluating whether this pattern fits your team.

1. **Read `docs/BEFORE-AFTER.md`** — the Big Ball of Mud comparison shows what hex arch prevents, not just what it adds.
2. **Read `docs/ARCHITECTURE.md` §When NOT to use hexagonal architecture** — honest guidance on when the overhead isn't worth it.
3. **Examine `AppConfig.java`** — all wiring in one place. This is the "adapter map" your team reviews in PRs.
4. **Look at the test pyramid**: 59 tests, zero Spring context needed. Domain tests run in <1s. Think about what that means for CI time.

### Management / non-technical
1. **Read `docs/BEFORE-AFTER.md`** — the business impact of architecture choices, explained without code.
2. **Key takeaway**: Hexagonal architecture means your team can swap AI providers, change databases, or add mobile channels without rewriting business logic. That's a risk management decision, not a technical preference.

## Running tests

```bash
mvn test
```

**59 tests** across 5 test classes:

| Test class | What it proves |
|---|---|
| `TalkTest` | Domain model state machine, guards, events, immutability |
| `SubmitTalkServiceTest` | Service orchestration, AI integration, fallback chain |
| `AdapterSwapTest` | Same domain behavior regardless of which AI adapter is plugged in |
| `AIFailureModeTest` | Timeout, outage, malformed response, full chain failure |
| `HexagonalArchitectureTest` | ArchUnit rules: domain has no Spring/JPA/infrastructure dependency |

## Project structure

```
src/main/java/com/dubjug/talksubmission/
├── domain/                          # Pure Java — no framework dependencies
│   ├── model/                       #   Talk (aggregate), Review, AIReviewResult, SubmissionStatus
│   └── event/                       #   Domain events: TalkSubmitted, ReviewAdded, TalkAccepted, TalkRejected
├── application/                     # Use cases and ports — depends only on domain
│   ├── port/in/                     #   Driving ports: SubmitTalkUseCase, ReviewTalkUseCase, GetTalkQuery
│   ├── port/out/                    #   Driven ports: TalkDataPort, AIPort, TalkEvaluationRequest
│   └── service/                     #   Orchestration: SubmitTalkService, ReviewTalkService
└── infrastructure/                  # Framework + external world — depends on application + domain
    ├── adapters/in/web/             #   REST controller (driving adapter)
    ├── adapters/in/mobile/          #   Mobile adapter (driving adapter)
    ├── adapters/out/ai/             #   OpenAI, Gemini, LocalRules, ResilientAIAdapter (driven adapters)
    └── adapters/out/persistence/    #   JPA, FileSystem, CloudStorage (driven adapters)
```

Dependency direction: `infrastructure → application → domain`. Never the reverse.

## Further reading

- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — deep dive into the architecture, dependency direction, AI as a port
- [`docs/BEFORE-AFTER.md`](docs/BEFORE-AFTER.md) — Big Ball of Mud vs hexagonal design comparison
- [`docs/EXERCISES.md`](docs/EXERCISES.md) — hands-on exercises by difficulty level
- [`docs/adr/`](docs/adr/) — Architecture Decision Records

## Tech stack

Java 17 · Spring Boot 3.3.5 · H2 (in-memory) · JUnit 5 · AssertJ · ArchUnit
