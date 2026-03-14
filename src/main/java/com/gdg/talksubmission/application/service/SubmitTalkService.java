package com.gdg.talksubmission.application.service;

import com.gdg.talksubmission.application.port.in.CreateDraftCommand;
import com.gdg.talksubmission.application.port.in.SubmitTalkUseCase;
import com.gdg.talksubmission.application.port.out.AIPort;
import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.AIReviewResult;
import com.gdg.talksubmission.domain.model.Review;
import com.gdg.talksubmission.domain.model.Talk;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SubmitTalkService implements SubmitTalkUseCase {
    private final TalkDataPort talkDataPort;
    private final AIPort primaryAIPort;
    private final AIPort secondaryAIPort;
    private final AIPort fallbackAIPort;

    public SubmitTalkService(TalkDataPort talkDataPort, AIPort primaryAIPort, AIPort secondaryAIPort, AIPort fallbackAIPort) {
        this.talkDataPort = talkDataPort;
        this.primaryAIPort = primaryAIPort;
        this.secondaryAIPort = secondaryAIPort;
        this.fallbackAIPort = fallbackAIPort;
    }

    @Override
    public Talk createDraft(CreateDraftCommand command) {
        Talk talk = Talk.draft(command.title(), command.abstractText(), command.speakerName());
        return talkDataPort.save(talk);
    }

    @Override
    public Talk submit(Long talkId) {
        Talk talk = talkDataPort.findById(talkId)
                .orElseThrow(() -> new IllegalArgumentException("Talk not found: " + talkId));

        talk.submit();
        AIReviewResult result = reviewWithFailover(talk);

        String feedback = buildFeedback(result);
        talk.addReview(new Review(result.getProvider(), feedback, result.isApproved(), Instant.now()));

        return talkDataPort.save(talk);
    }

    private AIReviewResult reviewWithFailover(Talk talk) {
        List<Exception> failures = new ArrayList<>();

        try {
            return primaryAIPort.evaluate(talk);
        } catch (Exception ex) {
            failures.add(ex);
        }

        try {
            return secondaryAIPort.evaluate(talk);
        } catch (Exception ex) {
            failures.add(ex);
        }

        try {
            return fallbackAIPort.evaluate(talk);
        } catch (Exception ex) {
            failures.add(ex);
            IllegalStateException exception = new IllegalStateException("All AI providers failed");
            failures.forEach(exception::addSuppressed);
            throw exception;
        }
    }

    private String buildFeedback(AIReviewResult result) {
        String concerns = result.getConcerns().isEmpty() ? "none" : String.join("; ", result.getConcerns());
        return result.getSummary() + " | concerns=" + concerns;
    }
}
