package com.gdg.talksubmission.application.port.in;

import com.gdg.talksubmission.domain.model.Talk;

public interface SubmitTalkUseCase {
    Talk createDraft(CreateDraftCommand command);
    Talk submit(Long talkId);
}
