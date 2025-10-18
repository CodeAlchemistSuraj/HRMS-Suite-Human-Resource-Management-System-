package com.hrms.auth.dto;

import com.hrms.auth.enums.MfaMethod;
import lombok.Data;

@Data
public class MfaEnableRequest {
    private MfaMethod method;
    private String verificationCode;
}