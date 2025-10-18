package com.hrms.auth.service.impl;

import com.hrms.auth.dto.*;
import com.hrms.auth.entity.RefreshToken;
import com.hrms.auth.entity.User;
import com.hrms.auth.entity.UserPrincipals;
import com.hrms.auth.enums.UserStatus;
import com.hrms.auth.events.PasswordResetRequestedEvent;
import com.hrms.auth.events.RefreshTokenRevokedEvent;
import com.hrms.auth.repo.RefreshTokenRepository;
import com.hrms.auth.repo.UserRepository;
import com.hrms.auth.service.*;
import com.hrms.auth.util.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final AuthAuditService authAuditService;
    private final MfaService mfaService;
    private final PasswordEncoderUtil passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername())
            .orElseThrow(() -> {
                authAuditService.recordLoginFailure(request.getUsername(), 
                    getClientIp(), getUserAgent(), "User not found");
                return new RuntimeException("Invalid credentials");
            });
        
        // Check user status
        if (user.getStatus() != UserStatus.ACTIVE) {
            authAuditService.recordLoginFailure(user.getUsername(), 
                getClientIp(), getUserAgent(), "Account " + user.getStatus());
            throw new RuntimeException("Account is " + user.getStatus());
        }
        
        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            user.incrementFailedLogin();
            userRepository.save(user);
            
            authAuditService.recordLoginFailure(user.getUsername(), 
                getClientIp(), getUserAgent(), "Invalid password");
            throw new RuntimeException("Invalid credentials");
        }
        
        // Validate MFA if enabled
        if (user.getMfaMethod() != MfaMethod.NONE) {
            if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                throw new RuntimeException("MFA code required");
            }
            
            MfaValidationRequest mfaRequest = new MfaValidationRequest();
            mfaRequest.setCode(request.getMfaCode());
            
            if (!mfaService.validateMfaCode(user.getId(), mfaRequest)) {
                authAuditService.recordLoginFailure(user.getUsername(), 
                    getClientIp(), getUserAgent(), "Invalid MFA code");
                throw new RuntimeException("Invalid MFA code");
            }
        }
        
        // Reset failed login counter
        user.resetFailedLogin();
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        
        // Create tokens
        UserPrincipals principals = createUserPrincipals(user);
        String accessToken = tokenService.createAccessToken(principals, Duration.ofMinutes(15));
        RefreshTokenDto refreshToken = tokenService.createRefreshToken(
            user.getId(), Duration.ofDays(30), getClientIp(), getUserAgent());
        
        authAuditService.recordLoginSuccess(user.getId(), getClientIp(), getUserAgent());
        
        return buildAuthResponse(accessToken, refreshToken, user);
    }
    
    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(
            TokenHashUtil.hashToken(refreshToken))
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }
        
        User user = userRepository.findById(storedToken.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User account is " + user.getStatus());
        }
        
        // Revoke old token (rotation)
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        
        // Create new tokens
        UserPrincipals principals = createUserPrincipals(user);
        String newAccessToken = tokenService.createAccessToken(principals, Duration.ofMinutes(15));
        RefreshTokenDto newRefreshToken = tokenService.createRefreshToken(
            user.getId(), Duration.ofDays(30), getClientIp(), getUserAgent());
        
        authAuditService.recordTokenRefresh(user.getId(), storedToken.getId());
        eventPublisher.publishEvent(new RefreshTokenRevokedEvent(this, storedToken.getId(), user.getId()));
        
        return buildAuthResponse(newAccessToken, newRefreshToken, user);
    }
    
    @Override
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(
            TokenHashUtil.hashToken(refreshToken))
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);
        
        eventPublisher.publishEvent(new RefreshTokenRevokedEvent(this, storedToken.getId(), storedToken.getUserId()));
    }
    
    @Override
    @Transactional
    public void revokeAllRefreshTokensForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        eventPublisher.publishEvent(new RefreshTokenRevokedEvent(this, null, userId));
    }
    
    @Override
    public TokenIntrospectionResponse introspectToken(String accessToken) {
        try {
            TokenClaims claims = tokenService.parseAndValidateAccessToken(accessToken);
            
            TokenIntrospectionResponse response = new TokenIntrospectionResponse();
            response.setActive(true);
            response.setUserId(claims.getSub());
            response.setRoles(claims.getRoles());
            response.setExp(claims.getExp());
            return response;
            
        } catch (Exception e) {
            TokenIntrospectionResponse response = new TokenIntrospectionResponse();
            response.setActive(false);
            return response;
        }
    }
    
    @Override
    public PasswordResetTokenDto initiatePasswordReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Generate reset token (implementation details would go here)
        PasswordResetTokenDto tokenDto = new PasswordResetTokenDto();
        tokenDto.setRequestId(UUID.randomUUID());
        tokenDto.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        tokenDto.setMaskedToken("****"); // Masked for security
        
        eventPublisher.publishEvent(new PasswordResetRequestedEvent(this, email, user.getId()));
        authAuditService.recordPasswordReset(user.getId());
        
        return tokenDto;
    }
    
    @Override
    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        // Validate token and update password
        // This would involve token validation logic
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // Update user password
        // user.setPasswordHash(encodedPassword);
        // userRepository.save(user);
        
        // Revoke all existing sessions
        // revokeAllRefreshTokensForUser(userId);
    }
    
    private UserPrincipals createUserPrincipals(User user) {
        // This would fetch actual roles from database
        return new UserPrincipals(user, java.util.List.of());
    }
    
    private AuthResponse buildAuthResponse(String accessToken, RefreshTokenDto refreshToken, User user) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setExpiresIn(Duration.ofMinutes(15));
        response.setRefreshToken(refreshToken.getToken());
        response.setIssuedAt(Instant.now());
        
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setStatus(user.getStatus());
        userDto.setEmployeeId(user.getEmployeeId());
        response.setUser(userDto);
        
        return response;
    }
    
    private String getClientIp() {
        // Implementation to get client IP from request context
        return "127.0.0.1";
    }
    
    private String getUserAgent() {
        // Implementation to get user agent from request context
        return "Unknown";
    }
}