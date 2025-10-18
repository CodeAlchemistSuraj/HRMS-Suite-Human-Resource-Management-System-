package com.hrms.auth.service;

import com.hrms.auth.dto.RoleCreateRequest;
import com.hrms.auth.dto.RoleDto;
import com.hrms.auth.enums.RoleName;

import java.util.List;
import java.util.UUID;

public interface RoleService {
    RoleDto createRole(RoleCreateRequest request);
    void deleteRole(UUID roleId);
    void assignRoleToUser(UUID userId, RoleName role);
    void revokeRoleFromUser(UUID userId, RoleName role);
    List<RoleDto> listRoles();
}