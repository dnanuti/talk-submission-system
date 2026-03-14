# Exercises

Hands-on exercises organised by difficulty. Each builds on the hexagonal architecture without requiring changes to the domain model unless stated.

## Beginner

### 1. Add a new failure trigger
**Goal**: Understand how AI adapter failure simulation works.

Add a `"low-confidence-openai"` trigger to `OpenAIAdapter` that returns an `AIReviewResult` with `approved=false` and a concern of `"Confidence score below threshold"`.

**Verify**: Write a test in `AIFailureModeTest` that asserts the result.

### 2. Add a status endpoint
**Goal**: Add a driving adapter without touching the domain.

Create a `GET /api/talks/{id}/status` endpoint that returns just `{"id": 1, "status": "UNDER_REVIEW"}`. Use the existing `GetTalkQuery` port.

**Verify**: `curl http://localhost:8080/api/talks/1/status` returns the right status after submission.

### 3. Write a test for Review validation
**Goal**: Understand value object invariants.

Write tests for the `Review` record's compact constructor — what happens with a blank reviewer? Null feedback? Null timestamp?

## Intermediate

### 4. Add a "withdraw" operation
**Goal**: Extend the domain state machine.

Add `talk.withdraw(Instant now)` that moves a `SUBMITTED` or `UNDER_REVIEW` talk back to `DRAFT`. It should raise a `TalkWithdrawn` domain event and clear existing reviews.

**Steps**:
1. Add the `withdraw()` method to `Talk.java`
2. Create a `TalkWithdrawn` event record
3. Add tests for valid transitions and guards (can't withdraw a draft, can't withdraw after decision)
4. Wire it through `ReviewTalkUseCase` and `TalkController`

### 5. Add a new AI adapter
**Goal**: Prove that adding a provider is purely an infrastructure concern.

Create a `ClaudeAdapter` that implements `AIPort`. It should approve talks with abstracts over 150 characters that mention "architecture" or "design". Add it to the resilient chain in `AppConfig`.

**Verify**: The existing `AdapterSwapTest` should pass with your new adapter added to the parameterised test.

### 6. Replace the persistence adapter
**Goal**: Prove the domain doesn't care about storage.

Modify `AppConfig` to wire `FileSystemTalkRepositoryAdapter` instead of `JpaTalkRepositoryAdapter`. Run the app and verify the full flow still works via curl.

## Advanced

### 7. Add event publishing
**Goal**: Use domain events to trigger side effects outside the aggregate.

Create a `DomainEventPublisher` port and adapter that logs (or prints) every domain event after the service saves the aggregate. The domain must not know about the publisher.

**Hint**: The service calls `talk.domainEvents()` after save, publishes them, then calls `talk.clearDomainEvents()`.

### 8. Add a confidence score to AI evaluation
**Goal**: Evolve the port contract without breaking existing adapters.

Add a `double confidence` field (0.0–1.0) to `AIReviewResult`. Update all adapters to return a confidence score. Add a business rule: if confidence < 0.5, the review is flagged for manual review regardless of the approved/rejected decision.

**Think about**: How do you add a field to a record without breaking existing tests? What does "flagged for manual review" mean in domain terms?

### 9. Implement the ArchUnit rule yourself
**Goal**: Understand how architectural tests work.

Delete `HexagonalArchitectureTest.java` and rewrite it from scratch. Add a rule that no class in `application.service` may directly instantiate any class from `infrastructure`. What would this catch that the existing rules don't?

### 10. Make it production-ready
**Goal**: Think about what's missing for real deployment.

This demo skips many production concerns. Pick two and implement them:
- **Concurrency**: What happens if two requests try to accept/reject the same talk? Add optimistic locking.
- **Observability**: Add structured logging to `ResilientAIAdapter` — log which provider failed, which succeeded, how long each attempt took.
- **Configuration**: Make the AI adapter chain configurable via `application.yml` instead of hardcoded in `AppConfig`.
- **API error handling**: Return proper HTTP 404/409 status codes instead of letting exceptions bubble to 500.
