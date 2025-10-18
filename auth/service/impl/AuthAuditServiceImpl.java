package com.hrms.auth.service.impl;

import com.hrms.auth.entity.AuthAuditLog;
import com.hrms.auth.repo.AuthAuditLogRepository;
import com.hrms.auth.service.AuthAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthAuditServiceImpl implements AuthAuditService {
    
    private final AuthAuditLogRepository authAuditLogRepository;
    
    @Override
    public void recordLoginSuccess(UUID userId, String ip, String userAgent) {
        AuthAuditLog auditLog = AuthAuditLog.builder()
            .actorUserId(userId)
            .action("LOGIN")
            .status("SUCCESS")
            .ipAddress(ip)
            .meta(buildMetaData("userAgent", userAgent))
            .build();
        
        authAuditLogRepository.save(auditLog);
    }
    
    @Override
    public void recordLoginFailure(String username, String ip, String userAgent, String reason) {
        AuthAuditLog auditLog = AuthAuditLog.builder()
            .action("LOGIN_FAILED")
            .status("FAILURE")
            .ipAddress(ip)
            .meta(buildMetaData("username", username, "userAgent", userAgent, "reason", reason))
            .build();
        
        authAuditLogRepository.save(auditLog);
    }
    
    @Override
    public void recordTokenRefresh(UUID userId, UUID refreshTokenId) {
        AuthAuditLog auditLog = AuthAuditLog.builder()
            .actorUserId(userId)
            .action("TOKEN_REFRESH")
            .status("SUCCESS")
            .meta(buildMetaData("refreshTokenId", refreshTokenId.toString()))
            .build();
        
        authAuditLogRepository.save(auditLog);
    }
    
    @Override
    public void recordPasswordReset(UUID userId) {
        AuthAuditLog auditLog = AuthAuditLog.builder()
            .actorUserId(userId)
            .action("PASSWORD_RESET")
            .status("SUCCESS")
            .build();
        
        authAuditLogRepository.save(auditLog);
    }
    
    private String buildMetaData(String... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key-values must be in pairs");
        }
        
        StringBuilder meta = new StringBuilder("{");
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) meta.append(",");
            meta.append("\"").append(keyValues[i]).append("\":\"")
                .append(keyValues[i + 1]).append("\"");
        }
        meta.append("}");
        return meta.toString();
    }
}