package com.dubjug.talksubmission.domain.event;

import java.time.Instant;

public record ReviewAdded(Long talkId, String reviewer, boolean approved, Instant occurredAt) implements DomainEvent {
}
