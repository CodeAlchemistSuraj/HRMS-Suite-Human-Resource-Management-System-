package com.hrms.auth.dto;

import com.hrms.auth.enums.RoleName;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class RoleDto {
    private UUID id;
    private RoleName name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}