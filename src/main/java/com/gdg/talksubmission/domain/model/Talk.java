package com.gdg.talksubmission.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Talk {
    private Long id;
    private final String title;
    private final String abstractText;
    private final String speakerName;
    private SubmissionStatus status;
    private final List<Review> reviews;

    public Talk(Long id, String title, String abstractText, String speakerName, SubmissionStatus status, List<Review> reviews) {
        this.id = id;
        this.title = requireText(title, "title");
        this.abstractText = requireText(abstractText, "abstractText");
        this.speakerName = requireText(speakerName, "speakerName");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.reviews = new ArrayList<>(Objects.requireNonNullElseGet(reviews, List::of));
    }

    public static Talk draft(String title, String abstractText, String speakerName) {
        return new Talk(null, title, abstractText, speakerName, SubmissionStatus.DRAFT, List.of());
    }

    public void submit() {
        if (status != SubmissionStatus.DRAFT) {
            throw new IllegalStateException("Only draft talks can be submitted");
        }
        status = SubmissionStatus.SUBMITTED;
    }

    public void addReview(Review review) {
        if (status == SubmissionStatus.DRAFT) {
            throw new IllegalStateException("Cannot review a draft talk");
        }
        reviews.add(Objects.requireNonNull(review, "review must not be null"));
        status = SubmissionStatus.UNDER_REVIEW;
        status = review.isApproved() ? SubmissionStatus.ACCEPTED : SubmissionStatus.REJECTED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getSpeakerName() {
        return speakerName;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
