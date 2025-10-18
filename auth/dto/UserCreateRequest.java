package com.hrms.auth.dto;

import com.hrms.auth.enums.RoleName;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserCreateRequest {
    private String username;
    private String email;
    private String password;
    private UUID employeeId;
    private List<RoleName> roles;
}