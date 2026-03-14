package com.gdg.talksubmission.infrastructure.adapters.out.ai;

import com.gdg.talksubmission.application.port.out.AIPort;
import com.gdg.talksubmission.domain.model.AIReviewResult;
import com.gdg.talksubmission.domain.model.Talk;

import java.util.List;

public class OpenAIAdapter implements AIPort {
    @Override
    public AIReviewResult evaluate(Talk talk) {
        String text = talk.getAbstractText().toLowerCase();
        if (text.contains("fail-openai")) {
            throw new IllegalStateException("Simulated OpenAI outage");
        }

        boolean approved = text.length() >= 100 && containsAny(text, "architecture", "spring", "java", "platform", "resilience");
        String summary = approved
                ? "OpenAI approved the talk as relevant and technically credible"
                : "OpenAI suggests adding more technical depth and audience value";

        return new AIReviewResult("OpenAIAdapter", approved, summary,
                approved ? List.of() : List.of("Needs more specificity", "Clarify practitioner takeaways"));
    }

    private boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) {
                return true;
            }
        }
        return false;
    }
}
