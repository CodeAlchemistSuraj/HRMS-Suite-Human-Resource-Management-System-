package com.hrms.auth.service.impl;

import com.hrms.auth.dto.RoleCreateRequest;
import com.hrms.auth.dto.RoleDto;
import com.hrms.auth.entity.Role;
import com.hrms.auth.entity.UserRole;
import com.hrms.auth.enums.RoleName;
import com.hrms.auth.repo.RoleRepository;
import com.hrms.auth.repo.UserRoleRepository;
import com.hrms.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    
    @Override
    @Transactional
    public RoleDto createRole(RoleCreateRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Role already exists: " + request.getName());
        }
        
        Role role = Role.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();
        
        Role savedRole = roleRepository.save(role);
        return convertToDto(savedRole);
    }
    
    @Override
    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found"));
        
        // Check if role is assigned to any users
        List<UserRole> userRoles = userRoleRepository.findByRoleId(roleId);
        if (!userRoles.isEmpty()) {
            throw new RuntimeException("Cannot delete role assigned to users");
        }
        
        roleRepository.delete(role);
    }
    
    @Override
    @Transactional
    public void assignRoleToUser(UUID userId, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        // Check if already assigned
        List<UserRole> existingRoles = userRoleRepository.findByUserId(userId);
        boolean alreadyAssigned = existingRoles.stream()
            .anyMatch(userRole -> userRole.getRoleId().equals(role.getId()) && userRole.getRevokedAt() == null);
        
        if (alreadyAssigned) {
            throw new RuntimeException("Role already assigned to user");
        }
        
        UserRole userRole = UserRole.builder()
            .userId(userId)
            .roleId(role.getId())
            .assignedAt(Instant.now())
            .build();
        
        userRoleRepository.save(userRole);
    }
    
    @Override
    @Transactional
    public void revokeRoleFromUser(UUID userId, RoleName roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
        
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        UserRole userRole = userRoles.stream()
            .filter(ur -> ur.getRoleId().equals(role.getId()) && ur.getRevokedAt() == null)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Role not assigned to user"));
        
        userRole.revoke();
        userRoleRepository.save(userRole);
    }
    
    @Override
    public List<RoleDto> listRoles() {
        return roleRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }
    
    private RoleDto convertToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        return dto;
    }
}