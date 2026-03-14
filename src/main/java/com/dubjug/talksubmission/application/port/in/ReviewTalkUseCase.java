package com.dubjug.talksubmission.application.port.in;

import com.dubjug.talksubmission.domain.model.Talk;

public interface ReviewTalkUseCase {
    Talk addManualReview(AddManualReviewCommand command);
    Talk accept(Long talkId);
    Talk reject(Long talkId, String reason);
}
