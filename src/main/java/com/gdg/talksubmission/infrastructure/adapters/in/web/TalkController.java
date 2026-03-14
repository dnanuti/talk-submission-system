package com.gdg.talksubmission.infrastructure.adapters.in.web;

import com.gdg.talksubmission.application.port.in.AddManualReviewCommand;
import com.gdg.talksubmission.application.port.in.CreateDraftCommand;
import com.gdg.talksubmission.application.port.in.GetTalkQuery;
import com.gdg.talksubmission.application.port.in.ReviewTalkUseCase;
import com.gdg.talksubmission.application.port.in.SubmitTalkUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/talks")
public class TalkController {
    private final SubmitTalkUseCase submitTalkUseCase;
    private final ReviewTalkUseCase reviewTalkUseCase;
    private final GetTalkQuery getTalkQuery;

    public TalkController(SubmitTalkUseCase submitTalkUseCase, ReviewTalkUseCase reviewTalkUseCase, GetTalkQuery getTalkQuery) {
        this.submitTalkUseCase = submitTalkUseCase;
        this.reviewTalkUseCase = reviewTalkUseCase;
        this.getTalkQuery = getTalkQuery;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TalkResponse createDraft(@Valid @RequestBody TalkRequest request) {
        return TalkResponse.from(submitTalkUseCase.createDraft(new CreateDraftCommand(
                request.title(), request.abstractText(), request.speakerName())));
    }

    @PostMapping("/{id}/submit")
    public TalkResponse submit(@PathVariable Long id) {
        return TalkResponse.from(submitTalkUseCase.submit(id));
    }

    @PostMapping("/{id}/reviews")
    public TalkResponse addManualReview(@PathVariable Long id, @Valid @RequestBody ManualReviewRequest request) {
        return TalkResponse.from(reviewTalkUseCase.addManualReview(new AddManualReviewCommand(
                id, request.reviewer(), request.feedback(), request.approved())));
    }

    @GetMapping("/{id}")
    public TalkResponse get(@PathVariable Long id) {
        return TalkResponse.from(getTalkQuery.getById(id));
    }

    @GetMapping
    public List<TalkResponse> list() {
        return getTalkQuery.listAll().stream().map(TalkResponse::from).toList();
    }
}
