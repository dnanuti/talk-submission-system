package com.dubjug.talksubmission.infrastructure.adapters.out.ai;

import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LocalRulesFallbackAdapter implements AIPort {
    private static final Pattern KEYWORDS = Pattern.compile(
            "\\b(java|spring|architecture|ddd|testing|resilience|cloud|gemini|openai)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern BANNED = Pattern.compile(
            "\\b(tbd|lorem ipsum|coming soon)\\b",
            Pattern.CASE_INSENSITIVE);

    @Override
    public AIReviewResult evaluate(TalkEvaluationRequest request) {
        List<String> concerns = new ArrayList<>();
        String text = request.abstractText();
        boolean enoughWords = text.trim().split("\\s+").length >= 15;
        boolean hasKeywords = KEYWORDS.matcher(text).find();
        boolean hasBanned = BANNED.matcher(text).find();

        if (!enoughWords) concerns.add("Abstract is too short");
        if (!hasKeywords) concerns.add("Missing technical signal words");
        if (hasBanned) concerns.add("Contains placeholder wording");

        boolean approved = enoughWords && hasKeywords && !hasBanned;
        String summary = approved
                ? "Local rules approved the talk after upstream provider failures"
                : "Local rules rejected: weak structure or missing technical keywords";
        return new AIReviewResult("LocalRulesFallbackAdapter", approved, summary, concerns);
    }
}
