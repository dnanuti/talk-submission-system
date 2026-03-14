package com.gdg.muddy.service;

import com.gdg.muddy.model.Talk;
import com.gdg.muddy.repository.TalkRepository;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import org.springframework.stereotype.Service;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

import java.util.List;

@Service
public class TalkService {

    private final TalkRepository talkRepository;
    private final OpenAiService openAiService;
    private final Client geminiClient;

    public TalkService(TalkRepository talkRepository) {
        this.talkRepository = talkRepository;
        this.openAiService = new OpenAiService(System.getenv("OPENAI_API_KEY"));
        this.geminiClient = Client.builder()
                .apiKey(System.getenv("GEMINI_API_KEY"))
                .build();
    }

    public Talk submitTalk(String title, String abstractText, String speaker) {
        if (abstractText.length() < 50) {
            throw new RuntimeException("Abstract too short");
        }

        Talk talk = new Talk(title, abstractText, speaker);
        String result;

        try {
            // This will now catch the Exceptions thrown by the methods below
            result = evaluateWithOpenAI(abstractText);
        } catch (Exception ex) {
            try {
                result = evaluateWithGemini(abstractText);
            } catch (Exception e) {
                result = evaluateWithLocalRules(abstractText);
            }
        }

        if (result != null && result.toLowerCase().contains("reject")) {
            talk.setStatus("REJECTED");
        } else {
            talk.setStatus("SUBMITTED");
        }

        return talkRepository.save(talk);
    }

    // Added 'throws Exception' to satisfy the compiler
    private String evaluateWithOpenAI(String text) throws Exception {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(List.of(
                        new ChatMessage("user", "Evaluate this conference abstract: " + text)
                ))
                .build();

        return openAiService.createChatCompletion(request)
                .getChoices()
                .get(0)
                .getMessage()
                .getContent();
    }

    // Added 'throws Exception' to satisfy the compiler for the Gemini SDK calls
    private String evaluateWithGemini(String text) throws Exception {
        GenerateContentResponse response = geminiClient.models.generateContent(
                "gemini-1.5-flash",
                "Evaluate this conference abstract: " + text,
                null
        );

        return response.text();
    }

    private String evaluateWithLocalRules(String text) {
        String lowerText = text.toLowerCase();
        if (lowerText.contains("architecture")
                || lowerText.contains("distributed")
                || lowerText.contains("cloud")) {
            return "accept";
        }
        return "reject";
    }
}