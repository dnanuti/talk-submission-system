package com.gdg.talksubmission.application.service;

import com.gdg.talksubmission.application.port.in.CreateDraftCommand;
import com.gdg.talksubmission.application.port.out.AIPort;
import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.AIReviewResult;
import com.gdg.talksubmission.domain.model.Talk;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SubmitTalkServiceTest {

    @Test
    void shouldFallBackToGeminiWhenOpenAIFails() {
        InMemoryTalkDataPort talkDataPort = new InMemoryTalkDataPort();
        AIPort openAi = talk -> { throw new IllegalStateException("openai down"); };
        AIPort gemini = talk -> new AIReviewResult("GeminiAdapter", true, "Gemini approved", List.of());
        AIPort fallback = talk -> new AIReviewResult("LocalRulesFallbackAdapter", false, "Fallback rejected", List.of("too short"));

        SubmitTalkService service = new SubmitTalkService(talkDataPort, openAi, gemini, fallback);
        Talk draft = service.createDraft(new CreateDraftCommand("Ports and Adapters", "A deep practical talk about architecture, java, spring, cloud, and resilience for teams.", "Diana"));

        Talk submitted = service.submit(draft.getId());
        assertThat(submitted.getReviews()).hasSize(1);
        assertThat(submitted.getReviews().get(0).getReviewer()).isEqualTo("GeminiAdapter");
        assertThat(submitted.getStatus().name()).isEqualTo("ACCEPTED");
    }

    @Test
    void shouldFallBackToLocalRulesWhenOpenAiAndGeminiFail() {
        InMemoryTalkDataPort talkDataPort = new InMemoryTalkDataPort();
        AIPort openAi = talk -> { throw new IllegalStateException("openai down"); };
        AIPort gemini = talk -> { throw new IllegalStateException("gemini down"); };
        AIPort fallback = talk -> new AIReviewResult("LocalRulesFallbackAdapter", true, "Local rules approved", List.of());

        SubmitTalkService service = new SubmitTalkService(talkDataPort, openAi, gemini, fallback);
        Talk draft = service.createDraft(new CreateDraftCommand("Reliable Review", "This talk covers architecture, java, testing, resilience, and cloud migration with practical examples for developers and tech leads.", "Diana"));

        Talk submitted = service.submit(draft.getId());
        assertThat(submitted.getReviews().get(0).getReviewer()).isEqualTo("LocalRulesFallbackAdapter");
        assertThat(submitted.getStatus().name()).isEqualTo("ACCEPTED");
    }

    static class InMemoryTalkDataPort implements TalkDataPort {
        private final Map<Long, Talk> storage = new HashMap<>();
        private long sequence = 0;

        @Override
        public Talk save(Talk talk) {
            if (talk.getId() == null) {
                talk.setId(++sequence);
            }
            storage.put(talk.getId(), talk);
            return talk;
        }

        @Override
        public Optional<Talk> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Talk> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public void deleteAll() {
            storage.clear();
        }
    }
}
