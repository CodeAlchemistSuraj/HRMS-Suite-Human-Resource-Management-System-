package com.hrms.auth.dto;

import com.hrms.auth.enums.MfaMethod;
import lombok.Data;

@Data
public class MfaProvisioningDto {
    private MfaMethod method;
    private String provisioningUri;
    private String secret;
}