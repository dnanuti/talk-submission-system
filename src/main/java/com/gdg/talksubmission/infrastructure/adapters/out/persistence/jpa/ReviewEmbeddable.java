package com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.Instant;

@Embeddable
public class ReviewEmbeddable {
    @Column(nullable = false)
    private String reviewer;

    @Column(nullable = false, length = 4000)
    private String feedback;

    @Column(nullable = false)
    private boolean approved;

    @Column(nullable = false)
    private Instant createdAt;

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
