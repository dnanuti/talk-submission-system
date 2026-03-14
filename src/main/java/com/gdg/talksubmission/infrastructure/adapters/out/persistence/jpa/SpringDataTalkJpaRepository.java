package com.gdg.talksubmission.infrastructure.adapters.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTalkJpaRepository extends JpaRepository<TalkJpaEntity, Long> {
}
