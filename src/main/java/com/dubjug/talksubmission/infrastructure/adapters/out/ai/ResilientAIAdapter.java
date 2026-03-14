package com.dubjug.talksubmission.infrastructure.adapters.out.ai;

import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkEvaluationRequest;
import com.dubjug.talksubmission.domain.model.AIReviewResult;

import java.util.ArrayList;
import java.util.List;

public class ResilientAIAdapter implements AIPort {
    private final List<AIPort> chain;

    public ResilientAIAdapter(List<AIPort> chain) {
        if (chain == null || chain.isEmpty()) {
            throw new IllegalArgumentException("At least one AI provider is required");
        }
        this.chain = List.copyOf(chain);
    }

    @Override
    public AIReviewResult evaluate(TalkEvaluationRequest request) {
        List<Exception> failures = new ArrayList<>();
        for (AIPort provider : chain) {
            try {
                return provider.evaluate(request);
            } catch (Exception ex) {
                failures.add(ex);
            }
        }
        IllegalStateException exception = new IllegalStateException("All AI providers failed");
        failures.forEach(exception::addSuppressed);
        throw exception;
    }
}
