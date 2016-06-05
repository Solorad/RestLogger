package com.morenkov.restlogger.repository;

import com.morenkov.restlogger.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author emorenkov
 */
public interface ApplicationRepository extends JpaRepository<Application, String> {
}
