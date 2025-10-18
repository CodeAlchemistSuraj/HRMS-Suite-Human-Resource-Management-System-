package com.hrms.auth.dto;

import com.hrms.auth.enums.MfaMethod;
import com.hrms.auth.enums.UserStatus;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String email;
    private UserStatus status;
    private MfaMethod mfaMethod;
}