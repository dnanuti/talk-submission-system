# GDG London Talk Submission Platform

Spring Boot 3.x + Java 21 Maven project demonstrating a **Hexagonal Architecture** aligned to the expanded diagram:

- **Input Port** with multiple driving adapters (web + mobile)
- **Test Port** with a test-support adapter
- **Data Port** with multiple implementations (JPA, file system, cloud-storage mock)
- **AI Port** with multiple implementations (**OpenAI**, **Gemini**, **LocalRulesFallback**)

## Highlights

- `domain` has **zero** dependencies on Spring, JPA, or external AI SDKs.
- Bean wiring lives in the root `AppConfig`.
- `SubmitTalkService` demonstrates resilience by trying:
  1. `OpenAIAdapter`
  2. `GeminiAdapter`
  3. `LocalRulesFallbackAdapter`
- Default persistence is `JpaTalkRepositoryAdapter` backed by H2.
- Alternate persistence adapters are included to match the expanded hexagonal example.

## Run

```bash
mvn spring-boot:run
```

## Example flow

### Create draft
```bash
curl -X POST http://localhost:8080/api/talks \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Hexagonal Architecture for Real Teams",
    "abstractText": "This session shows how ports and adapters help teams contain change, add AI resilience, and keep business logic clean in Spring Boot systems.",
    "speakerName": "Diana"
  }'
```

### Submit talk normally
```bash
curl -X POST http://localhost:8080/api/talks/1/submit
```

### Force OpenAI failure and let Gemini succeed
Create a talk whose abstract contains `fail-openai`.

### Force OpenAI and Gemini failure and hit local fallback
Create a talk whose abstract contains both `fail-openai` and `fail-gemini`.
