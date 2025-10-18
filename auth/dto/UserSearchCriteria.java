package com.hrms.auth.dto;

import com.hrms.auth.enums.UserStatus;
import lombok.Data;

@Data
public class UserSearchCriteria {
    private String username;
    private String email;
    private UserStatus status;
    private UUID employeeId;
}