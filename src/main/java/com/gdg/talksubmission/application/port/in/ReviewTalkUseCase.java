package com.gdg.talksubmission.application.port.in;

import com.gdg.talksubmission.domain.model.Talk;

public interface ReviewTalkUseCase {
    Talk addManualReview(AddManualReviewCommand command);
}
