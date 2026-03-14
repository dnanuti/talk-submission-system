package com.dubjug.talksubmission.domain.event;

import java.time.Instant;

public record TalkAccepted(Long talkId, Instant occurredAt) implements DomainEvent {
}
