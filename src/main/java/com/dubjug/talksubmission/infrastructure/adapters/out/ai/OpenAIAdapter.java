package com.dubjug.talksubmission.infrastructure.adapters.out.ai;

import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;

import java.util.List;

public class OpenAIAdapter implements AIPort {
    @Override
    public AIReviewResult evaluate(TalkEvaluationRequest request) {
        String text = request.abstractText().toLowerCase();
        if (text.contains("fail-openai")) {
            throw new IllegalStateException("Simulated OpenAI outage");
        }
        if (text.contains("timeout-openai")) {
            throw new IllegalStateException("Simulated OpenAI timeout after 30s");
        }

        boolean approved = text.length() >= 100
                && containsAny(text, "architecture", "spring", "java", "platform", "resilience");
        String summary = approved
                ? "OpenAI approved the talk as relevant and technically credible"
                : "OpenAI suggests adding more technical depth and audience value";

        return new AIReviewResult("OpenAIAdapter", approved, summary,
                approved ? List.of() : List.of("Needs more specificity", "Clarify practitioner takeaways"));
    }

    private boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) return true;
        }
        return false;
    }
}
