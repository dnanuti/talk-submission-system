# ADR 002: AI Evaluation as a Driven Port

## Status
Accepted

## Context
AI providers (OpenAI, Gemini, etc.) are volatile — they change APIs, go down, and produce unpredictable outputs. The domain model shouldn't know which provider is evaluating a talk or how the call is made.

## Decision
Define `AIPort` as a driven port interface in the application layer. AI adapters live in infrastructure. The port takes a `TalkEvaluationRequest` (not the full `Talk` aggregate) to enforce a clean boundary — the AI provider only sees what it needs.

## Consequences
- Swapping providers is a wiring change, not a domain change
- The `TalkEvaluationRequest` boundary prevents AI adapters from depending on aggregate internals
- AI failure modes (timeout, outage, malformed response) are infrastructure concerns handled by `ResilientAIAdapter`
- Domain tests don't need AI stubs — they test business rules in isolation
- Trade-off: the `TalkEvaluationRequest` mapping is an extra step, but it's a one-liner factory method
