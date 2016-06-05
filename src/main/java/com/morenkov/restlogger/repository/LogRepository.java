package com.morenkov.restlogger.repository;

import com.morenkov.restlogger.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author emorenkov
 */
public interface LogRepository extends JpaRepository<Log, Long> {
}
