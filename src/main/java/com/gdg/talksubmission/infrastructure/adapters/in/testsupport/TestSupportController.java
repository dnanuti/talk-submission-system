package com.gdg.talksubmission.infrastructure.adapters.in.testsupport;

import com.gdg.talksubmission.application.port.in.CreateDraftCommand;
import com.gdg.talksubmission.application.port.in.TestSupportUseCase;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
@RequestMapping("/internal/test-support")
public class TestSupportController {
    private final TestSupportUseCase testSupportUseCase;

    public TestSupportController(TestSupportUseCase testSupportUseCase) {
        this.testSupportUseCase = testSupportUseCase;
    }

    @DeleteMapping
    public void resetAll() {
        testSupportUseCase.resetAll();
    }

    @PostMapping("/seed")
    public long seed(@RequestBody SeedTalkRequest request) {
        return testSupportUseCase.seedTalk(new CreateDraftCommand(request.title(), request.abstractText(), request.speakerName()));
    }

    public record SeedTalkRequest(String title, String abstractText, String speakerName) {
    }
}
