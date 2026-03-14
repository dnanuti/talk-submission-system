package com.dubjug.talksubmission.domain.model;

import com.dubjug.talksubmission.domain.event.DomainEvent;
import com.dubjug.talksubmission.domain.event.ReviewAdded;
import com.dubjug.talksubmission.domain.event.TalkAccepted;
import com.dubjug.talksubmission.domain.event.TalkRejected;
import com.dubjug.talksubmission.domain.event.TalkSubmitted;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TalkTest {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    private static Talk draftTalk() {
        return Talk.draft(1L, "Hexagonal Architecture",
                "A deep dive into ports and adapters for real-world Java services",
                "Diana");
    }

    private static Talk submittedTalk() {
        Talk talk = draftTalk();
        talk.submitForReview(NOW);
        talk.clearDomainEvents();
        return talk;
    }

    private static Talk underReviewTalk() {
        Talk talk = submittedTalk();
        talk.addReview(approval("AI-Reviewer"));
        talk.clearDomainEvents();
        return talk;
    }

    private static Review approval(String reviewer) {
        return new Review(reviewer, "Looks great", true, NOW);
    }

    private static Review rejection(String reviewer) {
        return new Review(reviewer, "Needs more depth", false, NOW);
    }

    // --- Creation ---

    @Nested
    class Creation {
        @Test
        void draftFactoryAssignsIdentityAndDraftStatus() {
            Talk talk = Talk.draft(42L, "Title", "Abstract text here", "Speaker");
            assertThat(talk.getId()).isEqualTo(42L);
            assertThat(talk.getStatus()).isEqualTo(SubmissionStatus.DRAFT);
            assertThat(talk.getReviews()).isEmpty();
        }

        @Test
        void rejectsBlankTitle() {
            assertThatThrownBy(() -> Talk.draft(1L, "", "Abstract", "Speaker"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullAbstract() {
            assertThatThrownBy(() -> Talk.draft(1L, "Title", null, "Speaker"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsBlankSpeaker() {
            assertThatThrownBy(() -> Talk.draft(1L, "Title", "Abstract", "  "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // --- State machine: the flow a talk goes through ---

    @Nested
    class StateMachine {
        @Test
        void draftCanBeSubmittedForReview() {
            Talk talk = draftTalk();
            talk.submitForReview(NOW);
            assertThat(talk.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
        }

        @Test
        void cannotSubmitTwice() {
            Talk talk = submittedTalk();
            assertThatThrownBy(() -> talk.submitForReview(NOW))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotReviewADraft() {
            Talk talk = draftTalk();
            assertThatThrownBy(() -> talk.addReview(approval("Alice")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("draft");
        }

        @Test
        void firstReviewMovesToUnderReview() {
            Talk talk = submittedTalk();
            talk.addReview(approval("Alice"));
            assertThat(talk.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);
        }

        @Test
        void multipleReviewsStayUnderReview() {
            Talk talk = submittedTalk();
            talk.addReview(approval("AI"));
            talk.addReview(rejection("Human"));
            assertThat(talk.getStatus()).isEqualTo(SubmissionStatus.UNDER_REVIEW);
            assertThat(talk.getReviews()).hasSize(2);
        }

        @Test
        void canAcceptAfterReview() {
            Talk talk = underReviewTalk();
            talk.accept(NOW);
            assertThat(talk.getStatus()).isEqualTo(SubmissionStatus.ACCEPTED);
        }

        @Test
        void canRejectWithReason() {
            Talk talk = underReviewTalk();
            talk.reject("Topic already covered this year", NOW);
            assertThat(talk.getStatus()).isEqualTo(SubmissionStatus.REJECTED);
            assertThat(talk.getRejectionReason()).isEqualTo("Topic already covered this year");
        }

        @Test
        void cannotAcceptWithoutReview() {
            Talk talk = submittedTalk();
            assertThatThrownBy(() -> talk.accept(NOW))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("UNDER_REVIEW");
        }

        @Test
        void cannotRejectADraft() {
            Talk talk = draftTalk();
            assertThatThrownBy(() -> talk.reject("bad", NOW))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void cannotReviewAfterAccepted() {
            Talk talk = underReviewTalk();
            talk.accept(NOW);
            assertThatThrownBy(() -> talk.addReview(approval("Late reviewer")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("decision");
        }

        @Test
        void cannotAcceptAfterRejected() {
            Talk talk = underReviewTalk();
            talk.reject("nope", NOW);
            assertThatThrownBy(() -> talk.accept(NOW))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void rejectRequiresAReason() {
            Talk talk = underReviewTalk();
            assertThatThrownBy(() -> talk.reject("", NOW))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // --- Query methods ---

    @Nested
    class QueryMethods {
        @Test
        void approvalAndRejectionCounts() {
            Talk talk = submittedTalk();
            talk.addReview(approval("AI"));
            talk.addReview(rejection("Human-1"));
            talk.addReview(approval("Human-2"));
            assertThat(talk.approvalCount()).isEqualTo(2);
            assertThat(talk.rejectionCount()).isEqualTo(1);
        }

        @Test
        void hasReviewsIsFalseForNewTalk() {
            assertThat(submittedTalk().hasReviews()).isFalse();
        }

        @Test
        void isDecidedAfterAccept() {
            Talk talk = underReviewTalk();
            assertThat(talk.isDecided()).isFalse();
            talk.accept(NOW);
            assertThat(talk.isDecided()).isTrue();
        }
    }

    // --- Domain events ---

    @Nested
    class DomainEvents {
        @Test
        void submitRaisesTalkSubmittedEvent() {
            Talk talk = draftTalk();
            talk.submitForReview(NOW);
            assertThat(talk.domainEvents()).hasSize(1);
            TalkSubmitted event = (TalkSubmitted) talk.domainEvents().get(0);
            assertThat(event.talkId()).isEqualTo(1L);
            assertThat(event.title()).isEqualTo("Hexagonal Architecture");
        }

        @Test
        void addReviewRaisesReviewAddedEvent() {
            Talk talk = submittedTalk();
            talk.addReview(approval("Alice"));
            ReviewAdded event = (ReviewAdded) talk.domainEvents().get(0);
            assertThat(event.reviewer()).isEqualTo("Alice");
            assertThat(event.approved()).isTrue();
        }

        @Test
        void acceptRaisesTalkAcceptedEvent() {
            Talk talk = underReviewTalk();
            talk.accept(NOW);
            assertThat(talk.domainEvents()).hasSize(1);
            assertThat(talk.domainEvents().get(0)).isInstanceOf(TalkAccepted.class);
        }

        @Test
        void rejectRaisesTalkRejectedWithReason() {
            Talk talk = underReviewTalk();
            talk.reject("Duplicate topic", NOW);
            TalkRejected event = (TalkRejected) talk.domainEvents().get(0);
            assertThat(event.reason()).isEqualTo("Duplicate topic");
        }

        @Test
        void clearDomainEventsRemovesAll() {
            Talk talk = draftTalk();
            talk.submitForReview(NOW);
            talk.clearDomainEvents();
            assertThat(talk.domainEvents()).isEmpty();
        }
    }

    // --- Immutability ---

    @Nested
    class Immutability {
        @Test
        void reviewsListIsUnmodifiable() {
            Talk talk = underReviewTalk();
            assertThatThrownBy(() -> talk.getReviews().add(approval("Intruder")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void domainEventsListIsUnmodifiable() {
            Talk talk = draftTalk();
            talk.submitForReview(NOW);
            assertThatThrownBy(() -> talk.domainEvents().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
