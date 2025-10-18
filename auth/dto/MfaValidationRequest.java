package com.hrms.auth.dto;

import lombok.Data;

@Data
public class MfaValidationRequest {
    private String code;
}