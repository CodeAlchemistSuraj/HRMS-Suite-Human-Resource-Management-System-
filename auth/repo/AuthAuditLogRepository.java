package com.hrms.auth.repo;

import com.hrms.auth.entity.AuthAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface AuthAuditLogRepository extends JpaRepository<AuthAuditLog, UUID> {
    
    Page<AuthAuditLog> findByActorUserId(UUID userId, Pageable pageable);
    Page<AuthAuditLog> findByActionAndCreatedAtBetween(String action, Instant from, Instant to, Pageable pageable);
}