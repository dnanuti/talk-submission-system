# Before and After: Big Ball of Mud vs Hexagonal Architecture

## The Big Ball of Mud version

In a typical "just get it working" approach, a talk submission system might look like this:

```java
@RestController
public class TalkController {

    @Autowired
    private JdbcTemplate db;

    @PostMapping("/api/talks/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable Long id) {
        // Business logic, HTTP handling, database access, and AI calls
        // all tangled together in one method

        Map<String, Object> talk = db.queryForMap(
            "SELECT * FROM talks WHERE id = ?", id);

        if (!"DRAFT".equals(talk.get("status"))) {
            return ResponseEntity.badRequest().body("Already submitted");
        }

        // Direct HTTP call to OpenAI — what if it's down?
        String response = restTemplate.postForObject(
            "https://api.openai.com/v1/chat/completions",
            buildPrompt(talk), String.class);

        // Parse AI response inline — what if the format changes?
        JSONObject json = new JSONObject(response);
        boolean approved = json.getBoolean("approved");

        // Update database directly
        db.update("UPDATE talks SET status = ?, ai_approved = ? WHERE id = ?",
            approved ? "ACCEPTED" : "REJECTED", approved, id);

        return ResponseEntity.ok(talk);
    }
}
```

### What's wrong with this?

| Problem | Impact |
|---|---|
| Business rules live in the controller | Can't test without starting the web server |
| Database access is inline SQL | Can't test without a database |
| AI provider is hardcoded | Can't test without calling OpenAI; can't switch providers |
| No failure handling for AI | If OpenAI is down, submissions break |
| Status transitions aren't enforced | Nothing stops invalid transitions from other entry points |
| No domain events | Other systems can't react to state changes |

### The real cost

- **Testing**: Every test needs Spring context, database, and either a live OpenAI key or MockWebServer. Tests take seconds, not milliseconds.
- **Changing AI provider**: You rewrite the controller. Business logic and AI integration are the same code.
- **Adding a mobile client**: You duplicate the logic in a new controller, or force mobile through the same HTTP contract.
- **AI outage at 2am**: Submissions fail. There's no fallback. Users see 500 errors.

## The hexagonal version (this project)

```java
// Domain — pure Java, no framework dependencies
public class Talk {
    public void submitForReview(Instant now) {
        if (status != DRAFT) throw new IllegalStateException(...);
        status = SUBMITTED;
        domainEvents.add(new TalkSubmitted(id, title, speakerName, now));
    }

    public void addReview(Review review) { ... }
    public void accept(Instant now) { ... }
    public void reject(String reason, Instant now) { ... }
}

// Port — what the application needs from the outside world
public interface AIPort {
    AIReviewResult evaluate(TalkEvaluationRequest request);
}

// Service — orchestration only
public class SubmitTalkService {
    public Talk submit(Long talkId) {
        Talk talk = talkDataPort.findById(talkId).orElseThrow(...);
        talk.submitForReview(Instant.now(clock));
        AIReviewResult result = aiPort.evaluate(TalkEvaluationRequest.from(talk));
        talk.addReview(new Review(result.provider(), ...));
        return talkDataPort.save(talk);
    }
}

// Adapter — handles resilience, framework integration
public class ResilientAIAdapter implements AIPort {
    public AIReviewResult evaluate(TalkEvaluationRequest request) {
        for (AIPort provider : chain) {
            try { return provider.evaluate(request); }
            catch (Exception ex) { failures.add(ex); }
        }
        throw new IllegalStateException("All AI providers failed");
    }
}
```

### What changed?

| Concern | Big Ball of Mud | Hexagonal |
|---|---|---|
| Business rules | Scattered in controller | `Talk.java` — one file, self-enforcing |
| State transitions | Raw SQL updates | Explicit methods with guards |
| AI integration | Hardcoded HTTP call | `AIPort` interface, swappable |
| AI failure handling | None — 500 error | Resilient chain with fallback |
| Testing | Needs Spring + DB + AI | Pure unit tests, <1 second |
| Adding a channel | Duplicate controller logic | New driving adapter, same ports |
| Domain events | None | Built into every transition |
| Architecture enforcement | Verbal agreements | ArchUnit tests in CI |

### The trade-off

The hexagonal version has more files (40 vs ~5). More interfaces. More indirection. For a weekend project, that's overhead. For a system that will live in production, get handed between teams, and need to survive AI provider changes — it's insurance.

**The question isn't "is hexagonal better?" It's "does your system change in ways that hexagonal architecture protects against?"**

If your system has:
- Multiple integration points that change independently → yes
- Business rules that need to survive infrastructure swaps → yes
- A team that needs to work on different adapters in parallel → yes
- One database, one API, one developer, ships next week → probably not
