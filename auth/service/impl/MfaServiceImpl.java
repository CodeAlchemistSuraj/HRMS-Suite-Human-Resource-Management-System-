package com.hrms.auth.service.impl;

import com.hrms.auth.dto.MfaEnableRequest;
import com.hrms.auth.dto.MfaProvisioningDto;
import com.hrms.auth.dto.MfaValidationRequest;
import com.hrms.auth.entity.User;
import com.hrms.auth.enums.MfaMethod;
import com.hrms.auth.repo.UserRepository;
import com.hrms.auth.service.MfaService;
import com.hrms.auth.util.MfaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {
    
    private final UserRepository userRepository;
    private final MfaUtil mfaUtil;
    
    @Override
    public MfaProvisioningDto provisionMfa(UUID userId, MfaMethod method) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (method == MfaMethod.NONE) {
            throw new RuntimeException("Invalid MFA method");
        }
        
        String secret = mfaUtil.generateSecret();
        String provisioningUri = mfaUtil.getQrCodeImageUri(secret, user.getUsername(), "HRMS-Suite");
        
        MfaProvisioningDto provisioningDto = new MfaProvisioningDto();
        provisioningDto.setMethod(method);
        provisioningDto.setProvisioningUri(provisioningUri);
        provisioningDto.setSecret(secret); // One-time display only
        
        return provisioningDto;
    }
    
    @Override
    @Transactional
    public void enableMfa(UUID userId, MfaEnableRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate the verification code
        if (!mfaUtil.verifyCode(request.getVerificationCode(), user.getMfaSecret())) {
            throw new RuntimeException("Invalid verification code");
        }
        
        user.setMfaMethod(request.getMethod());
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void disableMfa(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setMfaMethod(MfaMethod.NONE);
        user.setMfaSecret(null);
        userRepository.save(user);
    }
    
    @Override
    public boolean validateMfaCode(UUID userId, MfaValidationRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getMfaMethod() == MfaMethod.NONE || user.getMfaSecret() == null) {
            throw new RuntimeException("MFA not enabled for user");
        }
        
        return mfaUtil.verifyCode(request.getCode(), user.getMfaSecret());
    }
}