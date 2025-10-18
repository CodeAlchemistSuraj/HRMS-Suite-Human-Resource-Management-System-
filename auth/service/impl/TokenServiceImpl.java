package com.hrms.auth.service.impl;

import com.hrms.auth.config.JwtConfigProperties;
import com.hrms.auth.dto.RefreshTokenDto;
import com.hrms.auth.entity.RefreshToken;
import com.hrms.auth.entity.UserPrincipals;
import com.hrms.auth.repo.RefreshTokenRepository;
import com.hrms.auth.security.JwtTokenProvider;
import com.hrms.auth.service.TokenClaims;
import com.hrms.auth.service.TokenService;
import com.hrms.auth.util.JwtUtil;
import com.hrms.auth.util.TokenHashUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfigProperties jwtConfig;
    
    @Override
    public String createAccessToken(UserPrincipals principals, Duration ttl) {
        Instant now = Instant.now();
        Instant expiry = now.plus(ttl);
        
        TokenClaims claims = new TokenClaims();
        claims.setSub(principals.getUserId());
        claims.setPreferredUsername(principals.getUsername());
        claims.setRoles(principals.getRoles());
        claims.setEmployeeId(principals.getEmployeeId());
        claims.setIat(now);
        claims.setExp(expiry);
        claims.setJti(UUID.randomUUID().toString());
        
        return jwtTokenProvider.generateToken(
            principals.getUserId().toString(),
            expiry,
            claims.getJti()
        );
    }
    
    @Override
    @Transactional
    public RefreshTokenDto createRefreshToken(UUID userId, Duration ttl, String ip, String userAgent) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = TokenHashUtil.hashToken(rawToken);
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(ttl);
        
        RefreshToken refreshToken = RefreshToken.builder()
            .userId(userId)
            .tokenHash(tokenHash)
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .ipAddress(ip)
            .userAgent(userAgent)
            .build();
        
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        
        RefreshTokenDto dto = new RefreshTokenDto();
        dto.setId(savedToken.getId());
        dto.setExpiresAt(savedToken.getExpiresAt());
        dto.setToken(rawToken); // Return raw token only on creation
        
        return dto;
    }
    
    @Override
    @Transactional
    public void revokeRefreshTokenById(UUID refreshTokenId) {
        RefreshToken refreshToken = refreshTokenRepository.findById(refreshTokenId)
            .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
    }
    
    @Override
    public TokenClaims parseAndValidateAccessToken(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        
        Claims claims = jwtTokenProvider.getClaims(token);
        return jwtUtil.extractTokenClaims(claims);
    }
    
    @Override
    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
}