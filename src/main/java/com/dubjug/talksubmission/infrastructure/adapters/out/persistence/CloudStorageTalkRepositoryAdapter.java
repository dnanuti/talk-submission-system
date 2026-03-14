package com.dubjug.talksubmission.infrastructure.adapters.out.persistence;

import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.domain.model.Talk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class CloudStorageTalkRepositoryAdapter implements TalkDataPort {
    private final Map<Long, Talk> simulatedCloudBucket = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public long nextId() {
        return sequence.incrementAndGet();
    }

    @Override
    public Talk save(Talk talk) {
        simulatedCloudBucket.put(talk.getId(), talk);
        return talk;
    }

    @Override
    public Optional<Talk> findById(Long id) {
        return Optional.ofNullable(simulatedCloudBucket.get(id));
    }

    @Override
    public List<Talk> findAll() {
        return new ArrayList<>(simulatedCloudBucket.values());
    }

    @Override
    public void deleteAll() {
        simulatedCloudBucket.clear();
    }
}
