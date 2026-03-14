package com.dubjug.talksubmission.application.service;

import com.dubjug.talksubmission.application.port.in.CreateDraftCommand;
import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;
import com.dubjug.talksubmission.domain.model.SubmissionStatus;
import com.dubjug.talksubmission.domain.model.Talk;
import com.dubjug.talksubmission.infrastructure.adapters.out.ai.ResilientAIAdapter;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubmitTalkServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void submitTriggersAIReviewAndMovesToUnderReview() {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        AIPort ai = req -> new AIReviewResult("TestAI", true, "Approved", List.of());
        SubmitTalkService service = new SubmitTalkService(dataPort, ai, FIXED_CLOCK);

        Talk draft = service.createDraft(new CreateDraftCommand("My Talk", "A talk about architecture and java", "Diana"));
        Talk submitted = service.submit(draft.getId());

        assertThat(submitted.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);
        assertThat(submitted.getReviews()).hasSize(1);
        assertThat(submitted.getReviews().get(0).reviewer()).isEqualTo("TestAI");
    }

    @Test
    void resilientAdapterFallsBackToGeminiWhenOpenAIFails() {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        AIPort openAi = req -> { throw new IllegalStateException("openai down"); };
        AIPort gemini = req -> new AIReviewResult("GeminiAdapter", true, "Gemini approved", List.of());
        AIPort fallback = req -> new AIReviewResult("Fallback", false, "Rejected", List.of("too short"));
        AIPort resilient = new ResilientAIAdapter(List.of(openAi, gemini, fallback));

        SubmitTalkService service = new SubmitTalkService(dataPort, resilient, FIXED_CLOCK);
        Talk draft = service.createDraft(new CreateDraftCommand("Ports and Adapters",
                "A deep practical talk about architecture, java, spring, cloud, and resilience for teams.",
                "Diana"));

        Talk submitted = service.submit(draft.getId());
        assertThat(submitted.getReviews().get(0).reviewer()).isEqualTo("GeminiAdapter");
    }

    @Test
    void resilientAdapterFallsBackToLocalRulesWhenAllRemoteFail() {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        AIPort openAi = req -> { throw new IllegalStateException("openai down"); };
        AIPort gemini = req -> { throw new IllegalStateException("gemini down"); };
        AIPort fallback = req -> new AIReviewResult("LocalRules", true, "Local rules approved", List.of());
        AIPort resilient = new ResilientAIAdapter(List.of(openAi, gemini, fallback));

        SubmitTalkService service = new SubmitTalkService(dataPort, resilient, FIXED_CLOCK);
        Talk draft = service.createDraft(new CreateDraftCommand("Reliable Review",
                "This talk covers architecture, java, testing, and resilience for developers.",
                "Diana"));

        Talk submitted = service.submit(draft.getId());
        assertThat(submitted.getReviews().get(0).reviewer()).isEqualTo("LocalRules");
    }

    @Test
    void allProvidersFailThrowsWithSuppressedCauses() {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        AIPort allFail = new ResilientAIAdapter(List.of(
                req -> { throw new IllegalStateException("provider-1 down"); },
                req -> { throw new IllegalStateException("provider-2 down"); }
        ));

        SubmitTalkService service = new SubmitTalkService(dataPort, allFail, FIXED_CLOCK);
        Talk draft = service.createDraft(new CreateDraftCommand("Doomed Talk", "Short abstract about java things", "Diana"));

        assertThatThrownBy(() -> service.submit(draft.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All AI providers failed")
                .satisfies(ex -> assertThat(ex.getSuppressed()).hasSize(2));
    }

    // --- Shared in-memory adapter for tests ---

    static class InMemoryTalkDataPort implements TalkDataPort {
        private final Map<Long, Talk> storage = new HashMap<>();
        private long sequence = 0;

        @Override public long nextId() { return ++sequence; }

        @Override
        public Talk save(Talk talk) {
            storage.put(talk.getId(), talk);
            return talk;
        }

        @Override public Optional<Talk> findById(Long id) { return Optional.ofNullable(storage.get(id)); }
        @Override public List<Talk> findAll() { return new ArrayList<>(storage.values()); }
        @Override public void deleteAll() { storage.clear(); }
    }
}
