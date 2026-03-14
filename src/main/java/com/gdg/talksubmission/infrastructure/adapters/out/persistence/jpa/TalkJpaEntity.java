package com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa;

import com.gdg.talksubmission.domain.model.SubmissionStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "talks")
public class TalkJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String abstractText;

    @Column(nullable = false)
    private String speakerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "talk_reviews", joinColumns = @JoinColumn(name = "talk_id"))
    @OrderColumn(name = "review_order")
    private List<ReviewEmbeddable> reviews = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAbstractText() { return abstractText; }
    public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
    public SubmissionStatus getStatus() { return status; }
    public void setStatus(SubmissionStatus status) { this.status = status; }
    public List<ReviewEmbeddable> getReviews() { return reviews; }
    public void setReviews(List<ReviewEmbeddable> reviews) { this.reviews = new ArrayList<>(reviews); }
}
