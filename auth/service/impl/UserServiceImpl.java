package com.hrms.auth.service.impl;

import com.hrms.auth.dto.*;
import com.hrms.auth.entity.User;
import com.hrms.auth.enums.UserStatus;
import com.hrms.auth.events.UserCreatedEvent;
import com.hrms.auth.events.UserDisabledEvent;
import com.hrms.auth.events.UserUpdatedEvent;
import com.hrms.auth.repo.UserRepository;
import com.hrms.auth.service.UserService;
import com.hrms.auth.util.PasswordEncoderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoderUtil passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    
    @Override
    @Transactional
    public UserDto createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .status(UserStatus.ACTIVE)
            .employeeId(request.getEmployeeId())
            .build();
        
        User savedUser = userRepository.save(user);
        
        eventPublisher.publishEvent(new UserCreatedEvent(this, savedUser.getId()));
        
        return convertToDto(savedUser);
    }
    
    @Override
    @Transactional
    public UserDto updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setEmail(request.getEmail());
        user.setStatus(request.getStatus());
        
        if (request.getMfaMethod() != null) {
            user.setMfaMethod(request.getMfaMethod());
        }
        
        User updatedUser = userRepository.save(user);
        
        eventPublisher.publishEvent(new UserUpdatedEvent(this, updatedUser.getId()));
        
        return convertToDto(updatedUser);
    }
    
    @Override
    public UserDto getUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDto(user);
    }
    
    @Override
    public Page<UserDto> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(this::convertToDto);
    }
    
    @Override
    @Transactional
    public void disableUser(UUID userId, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.disable();
        userRepository.save(user);
        
        eventPublisher.publishEvent(new UserDisabledEvent(this, userId));
    }
    
    @Override
    @Transactional
    public void enableUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.enable();
        userRepository.save(user);
        
        eventPublisher.publishEvent(new UserUpdatedEvent(this, userId));
    }
    
    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        // Revoke all refresh tokens for security
        // refreshTokenService.revokeAllByUserId(userId);
    }
    
    @Override
    public PasswordResetTokenDto initiatePasswordReset(String email) {
        // Implementation would generate reset token
        PasswordResetTokenDto tokenDto = new PasswordResetTokenDto();
        tokenDto.setRequestId(UUID.randomUUID());
        tokenDto.setExpiresAt(Instant.now().plus(java.time.Duration.ofHours(1)));
        tokenDto.setMaskedToken("****");
        return tokenDto;
    }
    
    @Override
    @Transactional
    public void completePasswordReset(String token, String newPassword) {
        // Validate token and update password
        // Implementation details
    }
    
    @Override
    @Transactional
    public void provisionSystemUser(String systemName) {
        User systemUser = User.builder()
            .username("system." + systemName)
            .email(systemName + "@system.hrms")
            .status(UserStatus.ACTIVE)
            .build();
        
        userRepository.save(systemUser);
    }
    
    @Override
    public void assignRoleToUser(UUID userId, RoleName role) {
        // Implementation would assign role via UserRoleRepository
    }
    
    @Override
    public void revokeRoleFromUser(UUID userId, RoleName role) {
        // Implementation would revoke role via UserRoleRepository
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setEmployeeId(user.getEmployeeId());
        // dto.setRoles(fetchUserRoles(user.getId()));
        return dto;
    }
}