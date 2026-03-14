package com.dubjug.muddy.repository;

import com.dubjug.muddy.model.Talk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TalkRepository extends JpaRepository<Talk, Long> {
}
