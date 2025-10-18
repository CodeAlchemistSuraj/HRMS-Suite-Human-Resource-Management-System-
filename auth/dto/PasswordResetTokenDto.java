package com.hrms.auth.dto;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PasswordResetTokenDto {
    private UUID requestId;
    private Instant expiresAt;
    private String maskedToken;
}