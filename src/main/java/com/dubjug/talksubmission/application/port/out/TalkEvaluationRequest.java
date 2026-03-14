package com.dubjug.talksubmission.application.port.out;

import com.dubjug.talksubmission.domain.model.Talk;

public record TalkEvaluationRequest(String title, String abstractText, String speakerName) {

    public static TalkEvaluationRequest from(Talk talk) {
        return new TalkEvaluationRequest(talk.getTitle(), talk.getAbstractText(), talk.getSpeakerName());
    }
}
