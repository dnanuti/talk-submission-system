package com.dubjug.muddy.service;

import com.dubjug.muddy.model.Talk;
import com.dubjug.muddy.model.TalkStatus;
import com.dubjug.muddy.repository.TalkRepository;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class TalkService {

    private static final Logger log = LoggerFactory.getLogger(TalkService.class);

    private final TalkRepository talkRepository;
    private final OpenAiService openAiService;
    private final Client geminiClient;

    public TalkService(TalkRepository talkRepository,
                       @Value("${openai.api-key}") String openAiKey,
                       @Value("${gemini.api-key}") String geminiKey) {
        this.talkRepository = talkRepository;
        this.openAiService = new OpenAiService(openAiKey);
        this.geminiClient = Client.builder().apiKey(geminiKey).build();
    }

    public Talk createDraft(String title, String abstractText, String speaker) {
        if (abstractText == null || abstractText.trim().length() < 50) {
            throw new IllegalArgumentException("Abstract must be at least 50 characters");
        }
        Talk talk = new Talk(title, abstractText, speaker);
        return talkRepository.save(talk);
    }

    public Talk submitForReview(Long talkId) {
        Talk talk = findById(talkId);
        if (talk.getStatus() != TalkStatus.DRAFT) {
            throw new IllegalStateException("Only draft talks can be submitted");
        }

        talk.setStatus(TalkStatus.SUBMITTED);
        talk.setSubmittedAt(Instant.now());

        String evaluation = evaluateAbstract(talk.getAbstractText());
        boolean approved = !evaluation.toLowerCase().contains("reject");

        talk.setAiReviewSummary(evaluation);
        talk.setAiApproved(approved);
        talk.setStatus(TalkStatus.UNDER_REVIEW);

        return talkRepository.save(talk);
    }

    public Talk addManualReview(Long talkId, String reviewer, String feedback, boolean approved) {
        Talk talk = findById(talkId);
        if (talk.getStatus() != TalkStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Talk must be under review to add a manual review");
        }

        talk.setManualReviewer(reviewer);
        talk.setManualReviewFeedback(feedback);
        talk.setManualApproved(approved);

        return talkRepository.save(talk);
    }

    public Talk accept(Long talkId) {
        Talk talk = findById(talkId);
        if (talk.getStatus() != TalkStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Talk must be under review to accept");
        }

        talk.setStatus(TalkStatus.ACCEPTED);
        talk.setDecidedAt(Instant.now());
        log.info("Talk '{}' accepted", talk.getTitle());

        return talkRepository.save(talk);
    }

    public Talk reject(Long talkId, String reason) {
        Talk talk = findById(talkId);
        if (talk.getStatus() != TalkStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Talk must be under review to reject");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        talk.setStatus(TalkStatus.REJECTED);
        talk.setRejectionReason(reason);
        talk.setDecidedAt(Instant.now());
        log.info("Talk '{}' rejected: {}", talk.getTitle(), reason);

        return talkRepository.save(talk);
    }

    public Talk findById(Long id) {
        return talkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Talk not found: " + id));
    }

    public List<Talk> findAll() {
        return talkRepository.findAll();
    }

    private String evaluateAbstract(String text) {
        try {
            String result = evaluateWithOpenAI(text);
            log.info("Talk evaluated by OpenAI");
            return result;
        } catch (Exception e) {
            log.warn("OpenAI unavailable, falling back to Gemini: {}", e.getMessage());
        }

        try {
            String result = evaluateWithGemini(text);
            log.info("Talk evaluated by Gemini (fallback)");
            return result;
        } catch (Exception e) {
            log.warn("Gemini unavailable, falling back to local rules: {}", e.getMessage());
        }

        log.info("Talk evaluated by local rules (all providers unavailable)");
        return evaluateWithLocalRules(text);
    }

    private String evaluateWithOpenAI(String text) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-4o")
                .messages(List.of(
                        new ChatMessage("system", "You are a conference talk reviewer. "
                                + "Respond with 'accept' or 'reject' followed by a brief reason."),
                        new ChatMessage("user", "Evaluate this conference abstract: " + text)
                ))
                .build();

        return openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }

    private String evaluateWithGemini(String text) throws Exception {
        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-2.0-flash",
                "You are a conference talk reviewer. Evaluate this abstract and respond with "
                        + "'accept' or 'reject' followed by a brief reason: " + text,
                null
        );
        return response.text();
    }

    private String evaluateWithLocalRules(String text) {
        String lower = text.toLowerCase();
        boolean hasTechnicalDepth = lower.contains("architecture")
                || lower.contains("distributed")
                || lower.contains("cloud")
                || lower.contains("java")
                || lower.contains("testing");
        boolean longEnough = text.trim().split("\\s+").length >= 15;

        if (hasTechnicalDepth && longEnough) {
            return "accept — meets minimum technical depth and length criteria";
        }
        return "reject — insufficient technical depth or too short";
    }
}
