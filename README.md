# Talk Submission Platform — "Big Ball of Mud" version

> **DUBJug — "Designing for Change"**
> This is the **before** branch. It implements the same features as the [hexagonal version](../../tree/dub-jug/main) but with everything coupled together in one service class. Compare the two to see what hexagonal architecture changes — and what it costs.

A Spring Boot application that handles conference talk submissions with AI-powered review.

Talks follow a lifecycle: **Draft → Submitted → Under Review → Accepted / Rejected**. On submission, an AI provider (OpenAI or Gemini) automatically evaluates the abstract. If both AI providers are unavailable, local heuristic rules are used as a fallback. Human reviewers can add manual reviews before the final accept/reject decision.

## Tech stack

Java 17 · Spring Boot 3.2.5 · Spring Data JPA · H2 · OpenAI SDK · Google Gemini SDK

## Configuration

Set your API keys in `application.yml` or via environment variables:

```yaml
openai:
  api-key: ${OPENAI_API_KEY:your-key-here}

gemini:
  api-key: ${GEMINI_API_KEY:your-key-here}
```

## Run

```bash
mvn spring-boot:run
```

## API

### Create a draft

```bash
curl -s -X POST http://localhost:8080/api/talks \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hexagonal Architecture for Real Teams",
       "abstractText":"This session shows how ports and adapters help teams contain change, add AI resilience, and keep business logic clean in Spring Boot systems.",
       "speaker":"Diana"}' | jq
```

### Submit for AI review

```bash
curl -s -X POST http://localhost:8080/api/talks/1/submit | jq
```

### Add a manual review

```bash
curl -s -X POST http://localhost:8080/api/talks/1/review \
  -H 'Content-Type: application/json' \
  -d '{"reviewer":"Alice","feedback":"Strong proposal, clear takeaways","approved":true}' | jq
```

### Accept

```bash
curl -s -X POST http://localhost:8080/api/talks/1/accept | jq
```

### Reject with reason

```bash
curl -s -X POST http://localhost:8080/api/talks/1/reject \
  -H 'Content-Type: application/json' \
  -d '{"reason":"Topic already covered this year"}' | jq
```

### Get a talk / list all

```bash
curl -s http://localhost:8080/api/talks/1 | jq
curl -s http://localhost:8080/api/talks | jq
```
