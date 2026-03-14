package com.dubjug.muddy.controller;

import com.dubjug.muddy.model.Talk;
import com.dubjug.muddy.service.TalkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/talks")
public class TalkController {

    private final TalkService talkService;

    public TalkController(TalkService talkService) {
        this.talkService = talkService;
    }

    @PostMapping
    public ResponseEntity<Talk> createDraft(@Valid @RequestBody TalkRequest request) {
        Talk talk = talkService.createDraft(request.title(), request.abstractText(), request.speaker());
        return ResponseEntity.status(HttpStatus.CREATED).body(talk);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<Talk> submit(@PathVariable Long id) {
        return ResponseEntity.ok(talkService.submitForReview(id));
    }

    @PostMapping("/{id}/review")
    public ResponseEntity<Talk> addManualReview(@PathVariable Long id,
                                                @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(
                talkService.addManualReview(id, request.reviewer(), request.feedback(), request.approved()));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Talk> accept(@PathVariable Long id) {
        return ResponseEntity.ok(talkService.accept(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Talk> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
        return ResponseEntity.ok(talkService.reject(id, request.reason()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Talk> findById(@PathVariable Long id) {
        return ResponseEntity.ok(talkService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Talk>> findAll() {
        return ResponseEntity.ok(talkService.findAll());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    record TalkRequest(String title, String abstractText, String speaker) {}
    record ReviewRequest(String reviewer, String feedback, boolean approved) {}
    record RejectRequest(String reason) {}
}
