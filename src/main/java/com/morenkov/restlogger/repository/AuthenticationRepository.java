package com.morenkov.restlogger.repository;

import com.morenkov.restlogger.entity.Authentication;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author emorenkov
 */
public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {
    @Query("SELECT a FROM Authentication a where application_id = :applicationId ORDER BY authentication_time DESC")
    public List<Authentication> findLastAppAuth(@Param("applicationId") String applicationId, Pageable pageable);
}
