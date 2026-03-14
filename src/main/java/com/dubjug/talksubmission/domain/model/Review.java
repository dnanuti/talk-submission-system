package com.dubjug.talksubmission.domain.model;

import java.time.Instant;
import java.util.Objects;

public record Review(String reviewer, String feedback, boolean approved, Instant createdAt) {

    public Review {
        if (reviewer == null || reviewer.isBlank()) {
            throw new IllegalArgumentException("reviewer must not be blank");
        }
        if (feedback == null || feedback.isBlank()) {
            throw new IllegalArgumentException("feedback must not be blank");
        }
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }
}
