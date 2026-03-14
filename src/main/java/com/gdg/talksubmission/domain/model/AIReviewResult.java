package com.gdg.talksubmission.domain.model;

import java.util.List;
import java.util.Objects;

public final class AIReviewResult {
    private final String provider;
    private final boolean approved;
    private final String summary;
    private final List<String> concerns;

    public AIReviewResult(String provider, boolean approved, String summary, List<String> concerns) {
        this.provider = requireText(provider, "provider");
        this.approved = approved;
        this.summary = requireText(summary, "summary");
        this.concerns = List.copyOf(Objects.requireNonNullElseGet(concerns, List::of));
    }

    public String getProvider() {
        return provider;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getConcerns() {
        return concerns;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
