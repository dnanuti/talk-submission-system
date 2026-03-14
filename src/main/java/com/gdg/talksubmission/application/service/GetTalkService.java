package com.gdg.talksubmission.application.service;

import com.gdg.talksubmission.application.port.in.GetTalkQuery;
import com.gdg.talksubmission.application.port.out.TalkDataPort;
import com.gdg.talksubmission.domain.model.Talk;

import java.util.List;

public class GetTalkService implements GetTalkQuery {
    private final TalkDataPort talkDataPort;

    public GetTalkService(TalkDataPort talkDataPort) {
        this.talkDataPort = talkDataPort;
    }

    @Override
    public Talk getById(Long talkId) {
        return talkDataPort.findById(talkId)
                .orElseThrow(() -> new IllegalArgumentException("Talk not found: " + talkId));
    }

    @Override
    public List<Talk> listAll() {
        return talkDataPort.findAll();
    }
}
