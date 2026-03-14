package com.dubjug.talksubmission.application.port.out;

import com.dubjug.talksubmission.domain.model.AIReviewResult;

public interface AIPort {
    AIReviewResult evaluate(TalkEvaluationRequest request);
}
