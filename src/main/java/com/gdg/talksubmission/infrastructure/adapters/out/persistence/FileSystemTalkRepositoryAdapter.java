package com.gdg.talksubmission.infrastructure.adapters.out.persistence;

import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.Talk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FileSystemTalkRepositoryAdapter implements TalkDataPort {
    private final Path filePath;
    private final Map<Long, Talk> talks = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public FileSystemTalkRepositoryAdapter(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public Talk save(Talk talk) {
        if (talk.getId() == null) {
            talk.setId(sequence.incrementAndGet());
        }
        talks.put(talk.getId(), talk);
        persistSnapshot();
        return talk;
    }

    @Override
    public Optional<Talk> findById(Long id) {
        return Optional.ofNullable(talks.get(id));
    }

    @Override
    public List<Talk> findAll() {
        return new ArrayList<>(talks.values());
    }

    @Override
    public void deleteAll() {
        talks.clear();
        persistSnapshot();
    }

    private void persistSnapshot() {
        try {
            Files.createDirectories(filePath.getParent());
            StringBuilder builder = new StringBuilder();
            for (Talk talk : talks.values()) {
                builder.append(talk.getId()).append('|')
                        .append(talk.getTitle()).append('|')
                        .append(talk.getSpeakerName()).append('|')
                        .append(talk.getStatus()).append(System.lineSeparator());
            }
            Files.writeString(filePath, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
            // demo adapter intentionally ignores local write failures
        }
    }
}
