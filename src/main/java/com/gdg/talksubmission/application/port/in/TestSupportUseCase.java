package com.gdg.talksubmission.application.port.in;

public interface TestSupportUseCase {
    void resetAll();
    long seedTalk(CreateDraftCommand command);
}
