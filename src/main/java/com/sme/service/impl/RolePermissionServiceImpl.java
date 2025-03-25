package com.sme.service.impl;

import com.sme.entity.Permission;
import com.sme.entity.Role;
import com.sme.entity.RolePermission;
import com.sme.repository.PermissionRepository;
import com.sme.repository.RolePermissionRepository;
import com.sme.repository.RoleRepository;
import com.sme.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Autowired
    public RolePermissionServiceImpl(RolePermissionRepository rolePermissionRepository,
                                     RoleRepository roleRepository,
                                     PermissionRepository permissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public RolePermission createRolePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById((long) roleId.intValue())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        // Check for duplicates
        if (rolePermissionRepository.findByRoleId(role.getId()).stream()
                .anyMatch(rp -> rp.getPermission().getId().equals(permissionId))) {
            throw new RuntimeException("Permission already assigned to this role");
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        return rolePermissionRepository.save(rolePermission);
    }

    @Override
    public List<RolePermission> getAllRolePermissions() {
        return rolePermissionRepository.findAll();
    }

    @Override
    public List<RolePermission> getRolePermissionsByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId((long) roleId.intValue());
    }

    @Override
    @Transactional
    public RolePermission updateRolePermission(Long id, Long newPermissionId) {
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RolePermission not found"));
        Permission newPermission = permissionRepository.findById(newPermissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        // Check if the new permission is already assigned to the role
        if (rolePermissionRepository.findByRoleId(rolePermission.getRole().getId()).stream()
                .anyMatch(rp -> rp.getPermission().getId().equals(newPermissionId) && !rp.getId().equals(id))) {
            throw new RuntimeException("New permission already assigned to this role");
        }

        rolePermission.setPermission(newPermission);
        return rolePermissionRepository.save(rolePermission);
    }

    @Override
    @Transactional
    public void deleteRolePermission(Long id) {
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RolePermission not found"));
        rolePermissionRepository.delete(rolePermission);
    }
}