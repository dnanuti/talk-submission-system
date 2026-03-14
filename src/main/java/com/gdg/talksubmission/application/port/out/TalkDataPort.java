package com.gdg.talksubmission.application.port.out;

import com.gdg.talksubmission.domain.model.Talk;

import java.util.List;
import java.util.Optional;

public interface TalkDataPort {
    Talk save(Talk talk);
    Optional<Talk> findById(Long id);
    List<Talk> findAll();
    void deleteAll();
}
