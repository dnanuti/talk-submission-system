package com.dubjug.talksubmission;

import com.dubjug.talksubmission.application.port.in.GetTalkQuery;
import com.dubjug.talksubmission.application.port.in.ReviewTalkUseCase;
import com.dubjug.talksubmission.application.port.in.SubmitTalkUseCase;
import com.dubjug.talksubmission.application.port.in.TestSupportUseCase;
import com.dubjug.talksubmission.application.port.out.AIPort;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.application.service.GetTalkService;
import com.dubjug.talksubmission.application.service.ReviewTalkService;
import com.dubjug.talksubmission.application.service.SubmitTalkService;
import com.dubjug.talksubmission.application.service.TestSupportService;
import com.dubjug.talksubmission.infrastructure.adapters.in.mobile.MobileTalkAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.ai.GeminiAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.ai.LocalRulesFallbackAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.ai.OpenAIAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.ai.ResilientAIAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.CloudStorageTalkRepositoryAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.FileSystemTalkRepositoryAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.JpaTalkRepositoryAdapter;
import com.dubjug.talksubmission.infrastructure.adapters.out.persistence.jpa.SpringDataTalkJpaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Clock;
import java.util.List;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    // --- AI adapters (out) ---

    @Bean("openAIAdapter")
    public AIPort openAIAdapter() {
        return new OpenAIAdapter();
    }

    @Bean("geminiAdapter")
    public AIPort geminiAdapter() {
        return new GeminiAdapter();
    }

    @Bean("localRulesFallbackAdapter")
    public AIPort localRulesFallbackAdapter() {
        return new LocalRulesFallbackAdapter();
    }

    @Bean("resilientAIAdapter")
    public AIPort resilientAIAdapter(
            @Qualifier("openAIAdapter") AIPort openAI,
            @Qualifier("geminiAdapter") AIPort gemini,
            @Qualifier("localRulesFallbackAdapter") AIPort fallback) {
        return new ResilientAIAdapter(List.of(openAI, gemini, fallback));
    }

    // --- Persistence adapters (out) ---

    @Bean("jpaTalkDataPort")
    public TalkDataPort jpaTalkDataPort(SpringDataTalkJpaRepository repository) {
        return new JpaTalkRepositoryAdapter(repository);
    }

    @Bean("fileSystemTalkDataPort")
    public TalkDataPort fileSystemTalkDataPort() {
        return new FileSystemTalkRepositoryAdapter(Path.of("build", "talks.json"));
    }

    @Bean("cloudStorageTalkDataPort")
    public TalkDataPort cloudStorageTalkDataPort() {
        return new CloudStorageTalkRepositoryAdapter();
    }

    // --- Application services ---

    @Bean
    public SubmitTalkUseCase submitTalkUseCase(
            @Qualifier("jpaTalkDataPort") TalkDataPort talkDataPort,
            @Qualifier("resilientAIAdapter") AIPort aiPort,
            Clock clock) {
        return new SubmitTalkService(talkDataPort, aiPort, clock);
    }

    @Bean
    public ReviewTalkUseCase reviewTalkUseCase(
            @Qualifier("jpaTalkDataPort") TalkDataPort talkDataPort,
            Clock clock) {
        return new ReviewTalkService(talkDataPort, clock);
    }

    @Bean
    public GetTalkQuery getTalkQuery(@Qualifier("jpaTalkDataPort") TalkDataPort talkDataPort) {
        return new GetTalkService(talkDataPort);
    }

    @Bean
    public TestSupportUseCase testSupportUseCase(@Qualifier("jpaTalkDataPort") TalkDataPort talkDataPort) {
        return new TestSupportService(talkDataPort);
    }

    @Bean
    public MobileTalkAdapter mobileTalkAdapter(SubmitTalkUseCase submitTalkUseCase) {
        return new MobileTalkAdapter(submitTalkUseCase);
    }
}
