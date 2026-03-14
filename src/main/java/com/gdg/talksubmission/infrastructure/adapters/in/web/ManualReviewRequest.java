package com.gdg.talksubmission.infrastructure.adapters.in.web;

import jakarta.validation.constraints.NotBlank;

public record ManualReviewRequest(
        @NotBlank String reviewer,
        @NotBlank String feedback,
        boolean approved) {
}
