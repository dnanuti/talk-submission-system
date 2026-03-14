package com.dubjug.talksubmission.infrastructure.adapters.out.ai;

import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;

import java.util.List;

public class GeminiAdapter implements AIPort {
    @Override
    public AIReviewResult evaluate(TalkEvaluationRequest request) {
        String text = request.abstractText().toLowerCase();
        if (text.contains("fail-gemini")) {
            throw new IllegalStateException("Simulated Gemini outage");
        }
        if (text.contains("malformed-gemini")) {
            throw new IllegalStateException("Simulated malformed Gemini response: missing 'approved' field");
        }

        boolean approved = text.length() >= 80
                && containsAny(text, "gemini", "google", "architecture", "cloud", "java", "testing", "resilience");
        String summary = approved
                ? "Gemini approved the talk as practical and well targeted"
                : "Gemini recommends more explicit outcomes and stronger technical framing";

        return new AIReviewResult("GeminiAdapter", approved, summary,
                approved ? List.of() : List.of("Outcomes are not explicit enough"));
    }

    private boolean containsAny(String text, String... tokens) {
        for (String token : tokens) {
            if (text.contains(token)) return true;
        }
        return false;
    }
}
