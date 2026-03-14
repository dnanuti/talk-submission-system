package com.dubjug.talksubmission.infrastructure.adapters.out.persistence;

import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.domain.model.Review;
import com.dubjug.talksubmission.domain.model.SubmissionStatus;
import com.dubjug.talksubmission.domain.model.Talk;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.jpa.ReviewEmbeddable;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.jpa.SpringDataTalkJpaRepository;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.jpa.TalkJpaEntity;

import java.util.List;
import java.util.Optional;

public class JpaTalkRepositoryAdapter implements TalkDataPort {
    private final SpringDataTalkJpaRepository repository;

    public JpaTalkRepositoryAdapter(SpringDataTalkJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public long nextId() {
        TalkJpaEntity placeholder = new TalkJpaEntity();
        placeholder.setTitle("__id_reservation__");
        placeholder.setAbstractText("__id_reservation__");
        placeholder.setSpeakerName("__id_reservation__");
        placeholder.setStatus(SubmissionStatus.DRAFT);
        TalkJpaEntity saved = repository.save(placeholder);
        repository.deleteById(saved.getId());
        return saved.getId();
    }

    @Override
    public Talk save(Talk talk) {
        TalkJpaEntity saved = repository.save(toEntity(talk));
        return toDomain(saved);
    }

    @Override
    public Optional<Talk> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Talk> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    private TalkJpaEntity toEntity(Talk talk) {
        TalkJpaEntity entity = new TalkJpaEntity();
        entity.setId(talk.getId());
        entity.setTitle(talk.getTitle());
        entity.setAbstractText(talk.getAbstractText());
        entity.setSpeakerName(talk.getSpeakerName());
        entity.setStatus(talk.getStatus());
        entity.setReviews(talk.getReviews().stream().map(this::toEmbeddable).toList());
        return entity;
    }

    private ReviewEmbeddable toEmbeddable(Review review) {
        ReviewEmbeddable e = new ReviewEmbeddable();
        e.setReviewer(review.reviewer());
        e.setFeedback(review.feedback());
        e.setApproved(review.approved());
        e.setCreatedAt(review.createdAt());
        return e;
    }

    private Talk toDomain(TalkJpaEntity entity) {
        List<Review> reviews = entity.getReviews().stream()
                .map(r -> new Review(r.getReviewer(), r.getFeedback(), r.isApproved(), r.getCreatedAt()))
                .toList();
        return new Talk(entity.getId(), entity.getTitle(), entity.getAbstractText(),
                entity.getSpeakerName(), entity.getStatus(), reviews);
    }
}
