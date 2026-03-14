package com.gdg.muddy.controller;

import com.gdg.muddy.model.Talk;
import com.gdg.muddy.service.TalkService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/talks")
public class TalkController {

    private final TalkService talkService;

    public TalkController(TalkService talkService) {
        this.talkService = talkService;
    }

    @PostMapping
    public Talk submit(@RequestBody Talk talk) {
        return talkService.submitTalk(
                talk.getTitle(),
                talk.getAbstractText(),
                talk.getSpeaker()
        );
    }
}
