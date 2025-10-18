package com.hrms.auth.service;

import com.hrms.auth.dto.MfaEnableRequest;
import com.hrms.auth.dto.MfaProvisioningDto;
import com.hrms.auth.dto.MfaValidationRequest;
import com.hrms.auth.enums.MfaMethod;

import java.util.UUID;

public interface MfaService {
    MfaProvisioningDto provisionMfa(UUID userId, MfaMethod method);
    void enableMfa(UUID userId, MfaEnableRequest request);
    void disableMfa(UUID userId);
    boolean validateMfaCode(UUID userId, MfaValidationRequest request);
}