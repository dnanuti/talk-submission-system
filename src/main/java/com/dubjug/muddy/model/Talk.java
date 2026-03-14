package com.dubjug.muddy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Entity
public class Talk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 2000)
    private String abstractText;

    @NotBlank
    @Column(nullable = false)
    private String speaker;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TalkStatus status;

    @Column(length = 2000)
    private String aiReviewSummary;

    private String aiReviewedBy;

    private Boolean aiApproved;

    @Column(length = 2000)
    private String manualReviewFeedback;

    private String manualReviewer;

    private Boolean manualApproved;

    @Column(length = 2000)
    private String rejectionReason;

    private Instant submittedAt;

    private Instant decidedAt;

    protected Talk() {}

    public Talk(String title, String abstractText, String speaker) {
        this.title = title;
        this.abstractText = abstractText;
        this.speaker = speaker;
        this.status = TalkStatus.DRAFT;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAbstractText() { return abstractText; }
    public String getSpeaker() { return speaker; }
    public TalkStatus getStatus() { return status; }
    public String getAiReviewSummary() { return aiReviewSummary; }
    public String getAiReviewedBy() { return aiReviewedBy; }
    public Boolean getAiApproved() { return aiApproved; }
    public String getManualReviewFeedback() { return manualReviewFeedback; }
    public String getManualReviewer() { return manualReviewer; }
    public Boolean getManualApproved() { return manualApproved; }
    public String getRejectionReason() { return rejectionReason; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Instant getDecidedAt() { return decidedAt; }

    public void setStatus(TalkStatus status) { this.status = status; }
    public void setAiReviewSummary(String aiReviewSummary) { this.aiReviewSummary = aiReviewSummary; }
    public void setAiReviewedBy(String aiReviewedBy) { this.aiReviewedBy = aiReviewedBy; }
    public void setAiApproved(Boolean aiApproved) { this.aiApproved = aiApproved; }
    public void setManualReviewFeedback(String feedback) { this.manualReviewFeedback = feedback; }
    public void setManualReviewer(String reviewer) { this.manualReviewer = reviewer; }
    public void setManualApproved(Boolean approved) { this.manualApproved = approved; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }
}
