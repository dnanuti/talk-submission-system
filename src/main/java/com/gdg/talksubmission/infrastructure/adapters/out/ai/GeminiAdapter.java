package com.gdg.talksubmission.infrastructure.adapters.out.ai;

import com.gdg.talksubmission.application.port.out.AIPort;
import com.gdg.talksubmission.domain.model.AIReviewResult;
import com.gdg.talksubmission.domain.model.Talk;

import java.util.List;

public class GeminiAdapter implements AIPort {
    @Override
    public AIReviewResult evaluate(Talk talk) {
        String text = talk.getAbstractText().toLowerCase();
        if (text.contains("fail-gemini")) {
            throw new IllegalStateException("Simulated Gemini outage");
        }

        boolean approved = text.length() >= 80 && containsAny(text, "gemini", "google", "architecture", "cloud", "java", "testing", "resilience");
        String summary = approved
                ? "Gemini approved the talk as practical and well targeted"
                : "Gemini recommends more explicit outcomes and stronger technical framing";

        return new AIReviewResult("GeminiAdapter", approved, summary,
                approved ? List.of() : List.of("Outcomes are not explicit enough"));
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
