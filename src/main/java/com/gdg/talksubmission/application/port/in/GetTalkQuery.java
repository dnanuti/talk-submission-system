package com.gdg.talksubmission.application.port.in;

import com.gdg.talksubmission.domain.model.Talk;

import java.util.List;

public interface GetTalkQuery {
    Talk getById(Long talkId);
    List<Talk> listAll();
}
