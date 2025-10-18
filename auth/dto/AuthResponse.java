package com.hrms.auth.dto;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;

@Data
public class AuthResponse {
    private String accessToken;
    private Duration expiresIn;
    private String refreshToken;
    private Instant issuedAt;
    private UserDto user;
}