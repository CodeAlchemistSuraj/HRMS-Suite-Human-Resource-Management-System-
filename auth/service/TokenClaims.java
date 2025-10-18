package com.hrms.auth.service;

import com.hrms.auth.enums.RoleName;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class TokenClaims {
    private UUID sub;
    private String preferredUsername;
    private List<RoleName> roles;
    private UUID employeeId;
    private Instant iat;
    private Instant exp;
    private String jti;
}