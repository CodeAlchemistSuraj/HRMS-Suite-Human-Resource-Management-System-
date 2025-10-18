package com.hrms.auth.entity;

import com.hrms.auth.enums.RoleName;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserPrincipals {
    private UUID userId;
    private String username;
    private String email;
    private List<RoleName> roles;
    private UUID employeeId;
    
    public UserPrincipals(User user, List<RoleName> roles) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.roles = roles;
        this.employeeId = user.getEmployeeId();
    }
}