package com.gdg.talksubmission.infrastructure.adapters.out.ai;

import com.gdg.talksubmission.application.port.out.AIPort;
import com.gdg.talksubmission.domain.model.AIReviewResult;
import com.gdg.talksubmission.domain.model.Talk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class LocalRulesFallbackAdapter implements AIPort {
    private static final Pattern KEYWORDS = Pattern.compile("\\b(java|spring|architecture|ddd|testing|resilience|cloud|gemini|openai)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern BANNED = Pattern.compile("\\b(tbd|lorem ipsum|coming soon)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public AIReviewResult evaluate(Talk talk) {
        List<String> concerns = new ArrayList<>();
        String text = talk.getAbstractText();
        boolean enoughWords = text.trim().split("\\s+").length >= 15;
        boolean hasKeywords = KEYWORDS.matcher(text).find();
        boolean hasBanned = BANNED.matcher(text).find();

        if (!enoughWords) {
            concerns.add("Abstract is too short");
        }
        if (!hasKeywords) {
            concerns.add("Missing technical signal words");
        }
        if (hasBanned) {
            concerns.add("Contains placeholder wording");
        }

        boolean approved = enoughWords && hasKeywords && !hasBanned;
        String summary = approved
                ? "Local rules fallback approved the talk after provider failures"
                : "Local rules fallback rejected the talk due to weak structure or missing technical keywords";
        return new AIReviewResult("LocalRulesFallbackAdapter", approved, summary, concerns);
    }
}
