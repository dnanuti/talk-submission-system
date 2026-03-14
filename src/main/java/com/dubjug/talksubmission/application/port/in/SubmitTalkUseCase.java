package com.dubjug.talksubmission.application.port.in;

import com.dubjug.talksubmission.domain.model.Talk;

public interface SubmitTalkUseCase {
    Talk createDraft(CreateDraftCommand command);
    Talk submit(Long talkId);
}
