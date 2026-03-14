package com.dubjug.talksubmission.domain.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
    Long talkId();
}
