package com.dubjug.talksubmission.application.service;

import com.dubjug.talksubmission.application.port.in.AddManualReviewCommand;
import com.dubjug.talksubmission.application.port.in.ReviewTalkUseCase;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.domain.model.Review;
import com.dubjug.talksubmission.domain.model.Talk;

import java.time.Clock;
import java.time.Instant;

public class ReviewTalkService implements ReviewTalkUseCase {
    private final TalkDataPort talkDataPort;
    private final Clock clock;

    public ReviewTalkService(TalkDataPort talkDataPort, Clock clock) {
        this.talkDataPort = talkDataPort;
        this.clock = clock;
    }

    @Override
    public Talk addManualReview(AddManualReviewCommand command) {
        Talk talk = findTalk(command.talkId());
        Instant now = Instant.now(clock);
        talk.addReview(new Review(command.reviewer(), command.feedback(), command.approved(), now));
        return talkDataPort.save(talk);
    }

    @Override
    public Talk accept(Long talkId) {
        Talk talk = findTalk(talkId);
        talk.accept(Instant.now(clock));
        return talkDataPort.save(talk);
    }

    @Override
    public Talk reject(Long talkId, String reason) {
        Talk talk = findTalk(talkId);
        talk.reject(reason, Instant.now(clock));
        return talkDataPort.save(talk);
    }

    private Talk findTalk(Long talkId) {
        return talkDataPort.findById(talkId)
                .orElseThrow(() -> new IllegalArgumentException("Talk not found: " + talkId));
    }
}
