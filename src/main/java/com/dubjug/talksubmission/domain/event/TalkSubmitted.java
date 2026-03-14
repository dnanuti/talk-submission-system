package com.dubjug.talksubmission.domain.event;

import java.time.Instant;

public record TalkSubmitted(Long talkId, String title, String speakerName, Instant occurredAt) implements DomainEvent {
}
