package com.hrms.auth.service;

import java.util.UUID;

public interface AuthAuditService {
    void recordLoginSuccess(UUID userId, String ip, String userAgent);
    void recordLoginFailure(String username, String ip, String userAgent, String reason);
    void recordTokenRefresh(UUID userId, UUID refreshTokenId);
    void recordPasswordReset(UUID userId);
}