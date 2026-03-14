package com.gdg.talksubmission.application.port.out;

import com.gdg.talksubmission.domain.model.AIReviewResult;
import com.gdg.talksubmission.domain.model.Talk;

public interface AIPort {
    AIReviewResult evaluate(Talk talk);
}
