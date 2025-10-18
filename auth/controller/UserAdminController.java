package com.hrms.auth.controller;

import com.hrms.auth.dto.*;
import com.hrms.auth.enums.RoleName;
import com.hrms.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserAdminController {
    
    private final UserService userService;
    
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR')")
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR') or hasRole('ROLE_MANAGER') or @userSecurity.isSelf(#id)")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUser(id));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR')")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.disableUser(id, "Deleted by admin");
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> assignRole(@PathVariable UUID id, @RequestBody RoleAssignmentRequest request) {
        userService.assignRoleToUser(id, request.getRole());
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> revokeRole(@PathVariable UUID id, @PathVariable RoleName roleName) {
        userService.revokeRoleFromUser(id, roleName);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_HR') or hasRole('ROLE_MANAGER')")
    public ResponseEntity<Page<UserDto>> searchUsers(UserSearchCriteria criteria, Pageable pageable) {
        return ResponseEntity.ok(userService.searchUsers(criteria, pageable));
    }
    
    @Data
    public static class RoleAssignmentRequest {
        private RoleName role;
    }
}