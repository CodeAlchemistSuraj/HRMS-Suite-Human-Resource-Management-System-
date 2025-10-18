package com.hrms.auth.service;

import com.hrms.auth.dto.*;

import java.util.UUID;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
    AuthResponse refreshToken(String refreshToken);
    void revokeRefreshToken(String refreshToken);
    void revokeAllRefreshTokensForUser(UUID userId);
    TokenIntrospectionResponse introspectToken(String accessToken);
}