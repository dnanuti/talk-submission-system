package com.gdg.talksubmission.infrastructure.adapters.out.persistence;

import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.Talk;

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
    public Talk save(Talk talk) {
        if (talk.getId() == null) {
            talk.setId(sequence.incrementAndGet());
        }
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
