package com.dubjug.talksubmission.application.service;

import com.dubjug.talksubmission.application.port.in.CreateDraftCommand;
import com.dubjug.talksubmission.application.port.in.SubmitTalkUseCase;
import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;
import com.dubjug.talksubmission.domain.model.Review;
import com.dubjug.talksubmission.domain.model.Talk;

import java.time.Clock;
import java.time.Instant;

public class SubmitTalkService implements SubmitTalkUseCase {
    private final TalkDataPort talkDataPort;
    private final AIPort aiPort;
    private final Clock clock;

    public SubmitTalkService(TalkDataPort talkDataPort, AIPort aiPort, Clock clock) {
        this.talkDataPort = talkDataPort;
        this.aiPort = aiPort;
        this.clock = clock;
    }

    @Override
    public Talk createDraft(CreateDraftCommand command) {
        long id = talkDataPort.nextId();
        Talk talk = Talk.draft(id, command.title(), command.abstractText(), command.speakerName());
        return talkDataPort.save(talk);
    }

    @Override
    public Talk submit(Long talkId) {
        Talk talk = talkDataPort.findById(talkId)
                .orElseThrow(() -> new IllegalArgumentException("Talk not found: " + talkId));

        Instant now = Instant.now(clock);
        talk.submitForReview(now);

        AIReviewResult result = aiPort.evaluate(TalkEvaluationRequest.from(talk));
        String feedback = formatFeedback(result);
        talk.addReview(new Review(result.provider(), feedback, result.approved(), now));

        return talkDataPort.save(talk);
    }

    private String formatFeedback(AIReviewResult result) {
        String concerns = result.concerns().isEmpty()
                ? "none"
                : String.join("; ", result.concerns());
        return result.summary() + " | concerns=" + concerns;
    }
}
