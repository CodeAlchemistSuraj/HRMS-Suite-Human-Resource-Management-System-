package com.hrms.auth.security;

import com.hrms.auth.config.JwtConfigProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    
    private final JwtConfigProperties jwtConfig;
    private SecretKey secretKey;
    
    private SecretKey getSecretKey() {
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        }
        return secretKey;
    }
    
    public String generateToken(String subject, Instant expiration, String jti) {
        return Jwts.builder()
            .setSubject(subject)
            .setIssuer(jwtConfig.getIssuer())
            .setIssuedAt(Date.from(Instant.now()))
            .setExpiration(Date.from(expiration))
            .setId(jti)
            .signWith(getSecretKey())
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature", e);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token", e);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token", e);
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty", e);
        }
        return false;
    }
    
    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSecretKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    public String getSubject(String token) {
        return getClaims(token).getSubject();
    }
    
    public Instant getExpiration(String token) {
        return getClaims(token).getExpiration().toInstant();
    }
    
    public String getJti(String token) {
        return getClaims(token).getId();
    }
}