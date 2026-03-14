package com.gdg.talksubmission.application.service;

import com.gdg.talksubmission.application.port.in.AddManualReviewCommand;
import com.gdg.talksubmission.application.port.in.ReviewTalkUseCase;
import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.Review;
import com.gdg.talksubmission.domain.model.Talk;

import java.time.Instant;

public class ReviewTalkService implements ReviewTalkUseCase {
    private final TalkDataPort talkDataPort;

    public ReviewTalkService(TalkDataPort talkDataPort) {
        this.talkDataPort = talkDataPort;
    }

    @Override
    public Talk addManualReview(AddManualReviewCommand command) {
        Talk talk = talkDataPort.findById(command.talkId())
                .orElseThrow(() -> new IllegalArgumentException("Talk not found: " + command.talkId()));
        talk.addReview(new Review(command.reviewer(), command.feedback(), command.approved(), Instant.now()));
        return talkDataPort.save(talk);
    }
}
