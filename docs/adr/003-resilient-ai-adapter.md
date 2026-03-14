# ADR 003: Resilient AI Adapter Using Composite Pattern

## Status
Accepted

## Context
AI providers fail. OpenAI has outages. Gemini returns malformed responses. We need a fallback strategy that doesn't leak into business logic.

## Decision
Use the composite pattern: `ResilientAIAdapter` implements `AIPort` and wraps an ordered list of `AIPort` implementations. It tries each in sequence, falling back on failure. If all fail, it throws with suppressed exceptions for debugging.

The chain is configured in `AppConfig`: OpenAI → Gemini → LocalRulesFallbackAdapter.

## Alternatives considered
- **Retry with backoff**: Adds latency. A conference submission doesn't need retry — just try the next provider.
- **Circuit breaker library (Resilience4j)**: Production-worthy, but adds a dependency and complexity that obscures the teaching point.
- **Single provider with error page**: Fails the demo when OpenAI is down.

## Consequences
- Each adapter is independently testable
- The chain is testable as a unit (`AIFailureModeTest`)
- `LocalRulesFallbackAdapter` always succeeds with heuristic rules — the system never fully fails
- Trade-off: no retry, no circuit state — fine for a demo, not enough for high-volume production traffic
