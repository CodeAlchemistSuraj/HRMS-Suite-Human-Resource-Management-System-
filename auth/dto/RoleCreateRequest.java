package com.hrms.auth.dto;

import com.hrms.auth.enums.RoleName;
import lombok.Data;

@Data
public class RoleCreateRequest {
    private RoleName name;
    private String description;
}