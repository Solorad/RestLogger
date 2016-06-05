package com.morenkov.restlogger.repository;

import com.morenkov.restlogger.entity.ApplicationProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author emorenkov
 */
public interface ApplicationPropertyRepository extends JpaRepository<ApplicationProperty, String> {

    @Cacheable("applicationId")
    public ApplicationProperty findOne(String id);
}
