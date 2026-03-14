package com.dubjug.talksubmission.infrastructure.adapters.in.web;

import jakarta.validation.constraints.NotBlank;

public record TalkRequest(
        @NotBlank String title,
        @NotBlank String abstractText,
        @NotBlank String speakerName) {
}
