package com.dubjug.talksubmission.application.port.in;

public interface TestSupportUseCase {
    void resetAll();
    long seedTalk(CreateDraftCommand command);
}
