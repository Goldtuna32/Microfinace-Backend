// RolePermissionService.java
package com.sme.service;

import com.sme.dto.RolePermissionDTO;
import com.sme.entity.RolePermission;
import java.util.List;

public interface RolePermissionService {
    RolePermissionDTO createRolePermission(Long roleId, Long permissionId);
    List<RolePermissionDTO> getAllRolePermissions();
    List<RolePermissionDTO> getRolePermissionsByRoleId(Long roleId);
    RolePermissionDTO updateRolePermission(Long id, Long newPermissionId);
    void deleteRolePermission(Long id);
}