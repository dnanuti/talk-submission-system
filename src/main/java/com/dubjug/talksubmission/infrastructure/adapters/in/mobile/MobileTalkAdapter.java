package com.dubjug.talksubmission.infrastructure.adapters.in.mobile;

import com.dubjug.talksubmission.application.port.in.CreateDraftCommand;
import com.dubjug.talksubmission.application.port.in.SubmitTalkUseCase;
import com.dubjug.talksubmission.domain.model.Talk;

public class MobileTalkAdapter {
    private final SubmitTalkUseCase submitTalkUseCase;

    public MobileTalkAdapter(SubmitTalkUseCase submitTalkUseCase) {
        this.submitTalkUseCase = submitTalkUseCase;
    }

    public Talk quickSubmit(String title, String abstractText, String speakerName) {
        Talk draft = submitTalkUseCase.createDraft(new CreateDraftCommand(title, abstractText, speakerName));
        return submitTalkUseCase.submit(draft.getId());
    }
}
