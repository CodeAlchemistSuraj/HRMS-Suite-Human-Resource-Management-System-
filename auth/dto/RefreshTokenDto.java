package com.hrms.auth.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class RefreshTokenDto {
    private UUID id;
    private Instant expiresAt;
    private String token;
}