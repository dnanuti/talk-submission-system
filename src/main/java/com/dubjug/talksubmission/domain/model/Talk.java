package com.dubjug.talksubmission.domain.model;

import com.dubjug.talksubmission.domain.event.DomainEvent;
import com.dubjug.talksubmission.domain.event.ReviewAdded;
import com.dubjug.talksubmission.domain.event.TalkAccepted;
import com.dubjug.talksubmission.domain.event.TalkRejected;
import com.dubjug.talksubmission.domain.event.TalkSubmitted;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Talk {

    private final Long id;
    private final String title;
    private final String abstractText;
    private final String speakerName;
    private SubmissionStatus status;
    private final List<Review> reviews;
    private String rejectionReason;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Talk(Long id, String title, String abstractText, String speakerName,
                SubmissionStatus status, List<Review> reviews) {
        this.id = id;
        this.title = requireText(title, "title");
        this.abstractText = requireText(abstractText, "abstractText");
        this.speakerName = requireText(speakerName, "speakerName");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.reviews = new ArrayList<>(Objects.requireNonNullElseGet(reviews, List::of));
    }

    public static Talk draft(Long id, String title, String abstractText, String speakerName) {
        return new Talk(id, title, abstractText, speakerName, SubmissionStatus.DRAFT, List.of());
    }

    public void submitForReview(Instant now) {
        if (status != SubmissionStatus.DRAFT) {
            throw new IllegalStateException("Only draft talks can be submitted for review");
        }
        status = SubmissionStatus.SUBMITTED;
        domainEvents.add(new TalkSubmitted(id, title, speakerName, now));
    }

    public void addReview(Review review) {
        if (status == SubmissionStatus.DRAFT) {
            throw new IllegalStateException("Cannot review a draft talk — submit it first");
        }
        if (isDecided()) {
            throw new IllegalStateException("Talk already has a decision: " + status);
        }
        reviews.add(Objects.requireNonNull(review, "review must not be null"));
        status = SubmissionStatus.UNDER_REVIEW;
        domainEvents.add(new ReviewAdded(id, review.reviewer(), review.approved(), review.createdAt()));
    }

    public void accept(Instant now) {
        requireUnderReview("accept");
        status = SubmissionStatus.ACCEPTED;
        domainEvents.add(new TalkAccepted(id, now));
    }

    public void reject(String reason, Instant now) {
        requireUnderReview("reject");
        this.rejectionReason = requireText(reason, "rejectionReason");
        status = SubmissionStatus.REJECTED;
        domainEvents.add(new TalkRejected(id, reason, now));
    }

    public boolean hasReviews() {
        return !reviews.isEmpty();
    }

    public boolean isDecided() {
        return status == SubmissionStatus.ACCEPTED || status == SubmissionStatus.REJECTED;
    }

    public long approvalCount() {
        return reviews.stream().filter(Review::approved).count();
    }

    public long rejectionCount() {
        return reviews.stream().filter(r -> !r.approved()).count();
    }

    public List<DomainEvent> domainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAbstractText() { return abstractText; }
    public String getSpeakerName() { return speakerName; }
    public SubmissionStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }

    public List<Review> getReviews() {
        return Collections.unmodifiableList(reviews);
    }

    private void requireUnderReview(String action) {
        if (status != SubmissionStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Cannot " + action + " a talk in status " + status + " — must be UNDER_REVIEW");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }
}
