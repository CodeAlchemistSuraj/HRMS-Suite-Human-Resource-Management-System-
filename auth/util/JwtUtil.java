package com.hrms.auth.util;

import com.hrms.auth.service.TokenClaims;
import com.hrms.auth.enums.RoleName;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    
    public TokenClaims extractTokenClaims(Claims claims) {
        TokenClaims tokenClaims = new TokenClaims();
        
        tokenClaims.setSub(UUID.fromString(claims.getSubject()));
        tokenClaims.setPreferredUsername(claims.get("preferred_username", String.class));
        
        // Extract roles
        List<String> rolesList = claims.get("roles", List.class);
        if (rolesList != null) {
            List<RoleName> roles = rolesList.stream()
                .map(role -> RoleName.valueOf(role))
                .collect(Collectors.toList());
            tokenClaims.setRoles(roles);
        }
        
        String employeeId = claims.get("employeeId", String.class);
        if (employeeId != null) {
            tokenClaims.setEmployeeId(UUID.fromString(employeeId));
        }
        
        tokenClaims.setIat(claims.getIssuedAt().toInstant());
        tokenClaims.setExp(claims.getExpiration().toInstant());
        tokenClaims.setJti(claims.getId());
        
        return tokenClaims;
    }
    
    public Claims buildClaims(TokenClaims tokenClaims) {
        Claims claims = Jwts.claims();
        
        claims.setSubject(tokenClaims.getSub().toString());
        claims.put("preferred_username", tokenClaims.getPreferredUsername());
        
        if (tokenClaims.getRoles() != null) {
            List<String> roles = tokenClaims.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toList());
            claims.put("roles", roles);
        }
        
        if (tokenClaims.getEmployeeId() != null) {
            claims.put("employeeId", tokenClaims.getEmployeeId().toString());
        }
        
        claims.setIssuedAt(Date.from(tokenClaims.getIat()));
        claims.setExpiration(Date.from(tokenClaims.getExp()));
        claims.setId(tokenClaims.getJti());
        
        return claims;
    }
}