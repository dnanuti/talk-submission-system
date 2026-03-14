package com.dubjug.talksubmission.application.service;

import com.dubjug.talksubmission.application.port.in.CreateDraftCommand;
import com.dubjug.talksubmission.application.port.in.TestSupportUseCase;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.domain.model.Talk;

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
        long id = talkDataPort.nextId();
        Talk talk = Talk.draft(id, command.title(), command.abstractText(), command.speakerName());
        return talkDataPort.save(talk).getId();
    }
}
