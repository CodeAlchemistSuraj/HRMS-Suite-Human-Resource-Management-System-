package com.hrms.auth.service;

import com.hrms.auth.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserDto createUser(UserCreateRequest request);
    UserDto updateUser(UUID userId, UserUpdateRequest request);
    UserDto getUser(UUID userId);
    Page<UserDto> searchUsers(UserSearchCriteria criteria, Pageable pageable);
    void disableUser(UUID userId, String reason);
    void enableUser(UUID userId);
    void changePassword(UUID userId, ChangePasswordRequest request);
    PasswordResetTokenDto initiatePasswordReset(String email);
    void completePasswordReset(String token, String newPassword);
    void provisionSystemUser(String systemName);
}