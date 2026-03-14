package com.dubjug.talksubmission.infrastructure.adapters.out.ai;

import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AIFailureModeTest {

    private static TalkEvaluationRequest request(String abstractText) {
        return new TalkEvaluationRequest("Test Talk", abstractText, "Diana");
    }

    @Nested
    class OpenAIFailures {
        private final AIPort openAI = new OpenAIAdapter();

        @Test
        void simulatesOutage() {
            assertThatThrownBy(() -> openAI.evaluate(request("fail-openai abstract")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("OpenAI outage");
        }

        @Test
        void simulatesTimeout() {
            assertThatThrownBy(() -> openAI.evaluate(request("timeout-openai abstract")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("timeout");
        }
    }

    @Nested
    class GeminiFailures {
        private final AIPort gemini = new GeminiAdapter();

        @Test
        void simulatesOutage() {
            assertThatThrownBy(() -> gemini.evaluate(request("fail-gemini abstract")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Gemini outage");
        }

        @Test
        void simulatesMalformedResponse() {
            assertThatThrownBy(() -> gemini.evaluate(request("malformed-gemini abstract")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("malformed");
        }
    }

    @Nested
    class LocalRulesFallback {
        private final AIPort local = new LocalRulesFallbackAdapter();

        @Test
        void rejectsTooShortAbstract() {
            AIReviewResult result = local.evaluate(request("too short"));
            assertThat(result.approved()).isFalse();
            assertThat(result.concerns()).contains("Abstract is too short");
        }

        @Test
        void rejectsPlaceholderContent() {
            AIReviewResult result = local.evaluate(request(
                    "This is a java architecture talk with spring and cloud and testing and resilience but lorem ipsum dolor sit amet"));
            assertThat(result.approved()).isFalse();
            assertThat(result.concerns()).contains("Contains placeholder wording");
        }

        @Test
        void approvesSolidAbstract() {
            AIReviewResult result = local.evaluate(request(
                    "A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers working with Spring and cloud environments."));
            assertThat(result.approved()).isTrue();
            assertThat(result.concerns()).isEmpty();
        }
    }

    @Nested
    class ResilientChain {
        @Test
        void fallsBackThroughFailingProviders() {
            AIPort chain = new ResilientAIAdapter(List.of(
                    req -> { throw new IllegalStateException("provider-1 down"); },
                    req -> { throw new IllegalStateException("provider-2 down"); },
                    req -> new AIReviewResult("Fallback", true, "Approved by fallback", List.of())
            ));
            AIReviewResult result = chain.evaluate(request("any abstract"));
            assertThat(result.provider()).isEqualTo("Fallback");
        }

        @Test
        void stopsAtFirstSuccessfulProvider() {
            AIPort chain = new ResilientAIAdapter(List.of(
                    req -> new AIReviewResult("Primary", true, "Primary approved", List.of()),
                    req -> { throw new AssertionError("Should not be called"); }
            ));
            AIReviewResult result = chain.evaluate(request("any abstract"));
            assertThat(result.provider()).isEqualTo("Primary");
        }

        @Test
        void allFailuresCollectSuppressedExceptions() {
            AIPort chain = new ResilientAIAdapter(List.of(
                    req -> { throw new IllegalStateException("fail-1"); },
                    req -> { throw new IllegalStateException("fail-2"); },
                    req -> { throw new IllegalStateException("fail-3"); }
            ));
            assertThatThrownBy(() -> chain.evaluate(request("any abstract")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("All AI providers failed")
                    .satisfies(ex -> assertThat(ex.getSuppressed()).hasSize(3));
        }

        @Test
        void openAIFailureTriggersGeminiFallback() {
            AIPort chain = new ResilientAIAdapter(List.of(
                    new OpenAIAdapter(),
                    new GeminiAdapter(),
                    new LocalRulesFallbackAdapter()
            ));
            AIReviewResult result = chain.evaluate(request(
                    "fail-openai A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers."));
            assertThat(result.provider()).isEqualTo("GeminiAdapter");
        }

        @Test
        void bothRemoteFailureTriggerLocalFallback() {
            AIPort chain = new ResilientAIAdapter(List.of(
                    new OpenAIAdapter(),
                    new GeminiAdapter(),
                    new LocalRulesFallbackAdapter()
            ));
            AIReviewResult result = chain.evaluate(request(
                    "fail-openai fail-gemini A deep dive into hexagonal architecture, DDD, and resilience patterns for Java platform engineers."));
            assertThat(result.provider()).isEqualTo("LocalRulesFallbackAdapter");
        }
    }
}
