package com.hrms.auth.dto;

import com.hrms.auth.enums.RoleName;
import com.hrms.auth.enums.UserStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String username;
    private String email;
    private UserStatus status;
    private List<RoleName> roles;
    private UUID employeeId;
}