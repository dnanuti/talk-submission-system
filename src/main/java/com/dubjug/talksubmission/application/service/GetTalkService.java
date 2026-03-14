package com.dubjug.talksubmission.application.service;

import com.dubjug.talksubmission.application.port.in.GetTalkQuery;
import com.dubjug.talksubmission.application.port.out.TalkDataPort;
import com.dubjug.talksubmission.domain.model.Talk;

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
