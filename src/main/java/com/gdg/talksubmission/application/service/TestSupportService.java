package com.gdg.talksubmission.application.service;

import com.gdg.talksubmission.application.port.in.CreateDraftCommand;
import com.gdg.talksubmission.application.port.in.TestSupportUseCase;
import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.Talk;

public class TestSupportService implements TestSupportUseCase {
    private final TalkDataPort talkDataPort;

    public TestSupportService(TalkDataPort talkDataPort) {
        this.talkDataPort = talkDataPort;
    }

    @Override
    public void resetAll() {
        talkDataPort.deleteAll();
    }

    @Override
    public long seedTalk(CreateDraftCommand command) {
        Talk talk = Talk.draft(command.title(), command.abstractText(), command.speakerName());
        Talk saved = talkDataPort.save(talk);
        return saved.getId();
    }
}
