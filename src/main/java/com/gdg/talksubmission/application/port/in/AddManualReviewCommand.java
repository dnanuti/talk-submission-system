package com.gdg.talksubmission.application.port.in;

public record AddManualReviewCommand(Long talkId, String reviewer, String feedback, boolean approved) {
}
