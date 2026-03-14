package com.gdg.talksubmission;

import com.gdg.talksubmission.application.port.in.GetTalkQuery;
import com.gdg.talksubmission.application.port.in.ReviewTalkUseCase;
import com.gdg.talksubmission.application.port.in.SubmitTalkUseCase;
import com.gdg.talksubmission.application.port.in.TestSupportUseCase;
import com.gdg.talksubmission.application.port.out.AIPort;
import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.application.service.GetTalkService;
import com.gdg.talksubmission.application.service.ReviewTalkService;
import com.gdg.talksubmission.application.service.SubmitTalkService;
import com.gdg.talksubmission.application.service.TestSupportService;
import com.gdg.talksubmission.infrastructure.adapters.out.ai.GeminiAdapter;
import com.gdg.talksubmission.infrastructure.adapters.in.mobile.MobileTalkAdapter;
import com.gdg.talksubmission.infrastructure.adapters.out.ai.LocalRulesFallbackAdapter;
import com.gdg.talksubmission.infrastructure.adapters.out.ai.OpenAIAdapter;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.CloudStorageTalkRepositoryAdapter;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.FileSystemTalkRepositoryAdapter;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.JpaTalkRepositoryAdapter;
import com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa.SpringDataTalkJpaRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class AppConfig {

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

    @Bean
    public SubmitTalkUseCase submitTalkUseCase(
            @Qualifier("jpaTalkDataPort") TalkDataPort talkDataPort,
            @Qualifier("openAIAdapter") AIPort openAIAdapter,
            @Qualifier("geminiAdapter") AIPort geminiAdapter,
            @Qualifier("localRulesFallbackAdapter") AIPort localRulesFallbackAdapter) {
        return new SubmitTalkService(talkDataPort, openAIAdapter, geminiAdapter, localRulesFallbackAdapter);
    }

    @Bean
    public ReviewTalkUseCase reviewTalkUseCase(@Qualifier("jpaTalkDataPort") TalkDataPort talkDataPort) {
        return new ReviewTalkService(talkDataPort);
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
