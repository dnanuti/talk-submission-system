package com.dubjug.talksubmission.infrastructure.adapters.in.web;

import com.dubjug.talksubmission.domain.model.Review;
import com.dubjug.talksubmission.domain.model.Talk;

import java.time.Instant;
import java.util.List;

public record TalkResponse(
        Long id,
        String title,
        String abstractText,
        String speakerName,
        String status,
        String rejectionReason,
        List<ReviewResponse> reviews) {

    public static TalkResponse from(Talk talk) {
        return new TalkResponse(
                talk.getId(),
                talk.getTitle(),
                talk.getAbstractText(),
                talk.getSpeakerName(),
                talk.getStatus().name(),
                talk.getRejectionReason(),
                talk.getReviews().stream().map(ReviewResponse::from).toList());
    }

    public record ReviewResponse(String reviewer, String feedback, boolean approved, Instant createdAt) {
        static ReviewResponse from(Review review) {
            return new ReviewResponse(review.reviewer(), review.feedback(), review.approved(), review.createdAt());
        }
    }
}
