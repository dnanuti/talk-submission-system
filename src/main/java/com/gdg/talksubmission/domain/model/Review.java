package com.gdg.talksubmission.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class Review {
    private final String reviewer;
    private final String feedback;
    private final boolean approved;
    private final Instant createdAt;

    public Review(String reviewer, String feedback, boolean approved, Instant createdAt) {
        this.reviewer = requireText(reviewer, "reviewer");
        this.feedback = requireText(feedback, "feedback");
        this.approved = approved;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public String getReviewer() {
        return reviewer;
    }

    public String getFeedback() {
        return feedback;
    }

    public boolean isApproved() {
        return approved;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
