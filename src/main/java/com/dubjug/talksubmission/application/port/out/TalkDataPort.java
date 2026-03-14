package com.dubjug.talksubmission.application.port.out;

import com.dubjug.talksubmission.domain.model.Talk;

import java.util.List;
import java.util.Optional;

public interface TalkDataPort {
    Talk save(Talk talk);
    Optional<Talk> findById(Long id);
    List<Talk> findAll();
    void deleteAll();
    long nextId();
}
