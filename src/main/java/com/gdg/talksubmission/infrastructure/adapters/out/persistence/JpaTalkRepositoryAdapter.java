package com.gdg.talksubmission.infrastructure.adapters.out.persistence;

import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.Review;
import com.gdg.talksubmission.domain.model.Talk;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa.ReviewEmbeddable;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa.SpringDataTalkJpaRepository;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa.TalkJpaEntity;

import java.util.List;
import java.util.Optional;

public class JpaTalkRepositoryAdapter implements TalkDataPort {
    private final SpringDataTalkJpaRepository repository;

    public JpaTalkRepositoryAdapter(SpringDataTalkJpaRepository repository) {
        this.repository = repository;
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
        ReviewEmbeddable embeddable = new ReviewEmbeddable();
        embeddable.setReviewer(review.getReviewer());
        embeddable.setFeedback(review.getFeedback());
        embeddable.setApproved(review.isApproved());
        embeddable.setCreatedAt(review.getCreatedAt());
        return embeddable;
    }

    private Talk toDomain(TalkJpaEntity entity) {
        List<Review> reviews = entity.getReviews().stream()
                .map(review -> new Review(review.getReviewer(), review.getFeedback(), review.isApproved(), review.getCreatedAt()))
                .toList();
        return new Talk(entity.getId(), entity.getTitle(), entity.getAbstractText(), entity.getSpeakerName(), entity.getStatus(), reviews);
    }
}
