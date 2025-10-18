package com.hrms.auth.dto;

import com.hrms.auth.enums.RoleName;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class TokenIntrospectionResponse {
    private boolean active;
    private UUID userId;
    private List<RoleName> roles;
    private Instant exp;
    private String scope;
}