package com.dubjug.talksubmission.infrastructure.adapters.out.ai;

import com.dubjug.talksubmission.application.port.in.AddManualReviewCommand;
import com.dubjug.talksubmission.application.port.in.CreateDraftCommand;
import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.application.service.ReviewTalkService;
import com.dubjug.talksubmission.application.service.SubmitTalkService;
import com.dubjug.talksubmission.domain.model.SubmissionStatus;
import com.dubjug.talksubmission.domain.model.Talk;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AdapterSwapTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-15T10:00:00Z"), ZoneOffset.UTC);

    static Stream<AIPort> aiAdapters() {
        return Stream.of(
                new OpenAIAdapter(),
                new GeminiAdapter(),
                new LocalRulesFallbackAdapter()
        );
    }

    @ParameterizedTest(name = "adapter: {0}")
    @MethodSource("aiAdapters")
    void submittedTalkMovesToUnderReview_regardlessOfAIAdapter(AIPort aiAdapter) {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        SubmitTalkService service = new SubmitTalkService(dataPort, aiAdapter, FIXED_CLOCK);

        Talk draft = service.createDraft(new CreateDraftCommand(
                "Designing for Change",
                "A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers.",
                "Diana"));

        Talk submitted = service.submit(draft.getId());

        assertThat(submitted.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);
        assertThat(submitted.getReviews()).hasSize(1);
    }

    @ParameterizedTest(name = "adapter: {0}")
    @MethodSource("aiAdapters")
    void acceptAndRejectWorkAfterAIReview_regardlessOfAdapter(AIPort aiAdapter) {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        SubmitTalkService submitService = new SubmitTalkService(dataPort, aiAdapter, FIXED_CLOCK);
        ReviewTalkService reviewService = new ReviewTalkService(dataPort, FIXED_CLOCK);

        Talk draft = submitService.createDraft(new CreateDraftCommand(
                "Designing for Change",
                "A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers.",
                "Diana"));

        Talk submitted = submitService.submit(draft.getId());
        assertThat(submitted.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);

        Talk accepted = reviewService.accept(submitted.getId());
        assertThat(accepted.getStatus()).isEqualTo(SubmissionStatus.ACCEPTED);
    }

    @ParameterizedTest(name = "adapter: {0}")
    @MethodSource("aiAdapters")
    void manualReviewCanBeAddedAfterAIReview_regardlessOfAdapter(AIPort aiAdapter) {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        SubmitTalkService submitService = new SubmitTalkService(dataPort, aiAdapter, FIXED_CLOCK);
        ReviewTalkService reviewService = new ReviewTalkService(dataPort, FIXED_CLOCK);

        Talk draft = submitService.createDraft(new CreateDraftCommand(
                "Designing for Change",
                "A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers.",
                "Diana"));

        Talk submitted = submitService.submit(draft.getId());

        Talk reviewed = reviewService.addManualReview(
                new AddManualReviewCommand(submitted.getId(), "Human Reviewer", "Strong talk proposal", true));

        assertThat(reviewed.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);
        assertThat(reviewed.getReviews()).hasSize(2);
    }

    @ParameterizedTest(name = "adapter: {0}")
    @MethodSource("aiAdapters")
    void domainInvariantsHold_regardlessOfAIAdapter(AIPort aiAdapter) {
        InMemoryTalkDataPort dataPort = new InMemoryTalkDataPort();
        SubmitTalkService service = new SubmitTalkService(dataPort, aiAdapter, FIXED_CLOCK);

        Talk draft = service.createDraft(new CreateDraftCommand(
                "Designing for Change",
                "A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers.",
                "Diana"));

        assertThat(draft.getStatus()).isEqualTo(SubmissionStatus.DRAFT);

        Talk submitted = service.submit(draft.getId());
        assertThat(submitted.getReviews().get(0).reviewer()).isNotBlank();
        assertThat(submitted.getReviews().get(0).feedback()).isNotBlank();
        assertThat(submitted.domainEvents()).isNotEmpty();
    }

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
