package com.dubjug.talksubmission.domain.event;

import java.time.Instant;

public record TalkRejected(Long talkId, String reason, Instant occurredAt) implements DomainEvent {
}
