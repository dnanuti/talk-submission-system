package com.dubjug.talksubmission.application.port.in;

import com.dubjug.talksubmission.domain.model.Talk;

import java.util.List;

public interface GetTalkQuery {
    Talk getById(Long talkId);
    List<Talk> listAll();
}
