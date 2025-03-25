// RolePermissionService.java
package com.sme.service;

import com.sme.entity.RolePermission;
import java.util.List;

public interface RolePermissionService {
    RolePermission createRolePermission(Long roleId, Long permissionId);
    List<RolePermission> getAllRolePermissions();
    List<RolePermission> getRolePermissionsByRoleId(Long roleId);
    RolePermission updateRolePermission(Long id, Long newPermissionId);
    void deleteRolePermission(Long id);
}