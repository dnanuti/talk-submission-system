package com.gdg.muddy.repository;

import com.gdg.muddy.model.Talk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkRepository extends JpaRepository<Talk, Long> {
}
