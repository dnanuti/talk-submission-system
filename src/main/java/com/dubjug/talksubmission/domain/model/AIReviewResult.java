package com.dubjug.talksubmission.domain.model;

import java.util.List;
import java.util.Objects;

public record AIReviewResult(String provider, boolean approved, String summary, List<String> concerns) {

    public AIReviewResult {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider must not be blank");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("summary must not be blank");
        }
        concerns = List.copyOf(Objects.requireNonNullElseGet(concerns, List::of));
    }
}
