package com.sme.service.impl;

import com.sme.dto.PermissionDTO;
import com.sme.dto.RoleDTO;
import com.sme.dto.RolePermissionDTO;
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
import java.util.stream.Collectors;

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

    private RoleDTO toRoleDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
        roleDTO.setDescription(role.getDescription());
        // Permissions might not be needed here unless specifically requested
        return roleDTO;
    }

    private PermissionDTO toPermissionDTO(Permission permission) {
        PermissionDTO permissionDTO = new PermissionDTO();
        permissionDTO.setId(permission.getId());
        permissionDTO.setName(permission.getName());
        permissionDTO.setPermissionFunction(permission.getPermissionFunction());
        permissionDTO.setDescription(permission.getDescription());
        return permissionDTO;
    }

    private RolePermissionDTO toRolePermissionDTO(RolePermission rolePermission) {
        RolePermissionDTO dto = new RolePermissionDTO();
        dto.setId(rolePermission.getId());
        dto.setRole(toRoleDTO(rolePermission.getRole()));
        dto.setPermission(toPermissionDTO(rolePermission.getPermission()));
        return dto;
    }

    @Override
    @Transactional
    public RolePermissionDTO createRolePermission(Long roleId, Long permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if (rolePermissionRepository.findByRoleId(role.getId()).stream()
                .anyMatch(rp -> rp.getPermission().getId().equals(permissionId))) {
            throw new RuntimeException("Permission already assigned to this role");
        }

        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        RolePermission saved = rolePermissionRepository.save(rolePermission);
        return toRolePermissionDTO(saved);
    }

    @Override
    public List<RolePermissionDTO> getAllRolePermissions() {
        return rolePermissionRepository.findAll().stream()
                .map(this::toRolePermissionDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RolePermissionDTO> getRolePermissionsByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(this::toRolePermissionDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RolePermissionDTO updateRolePermission(Long id, Long newPermissionId) {
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RolePermission not found"));
        Permission newPermission = permissionRepository.findById(newPermissionId)
                .orElseThrow(() -> new RuntimeException("Permission not found"));

        if (rolePermissionRepository.findByRoleId(rolePermission.getRole().getId()).stream()
                .anyMatch(rp -> rp.getPermission().getId().equals(newPermissionId) && !rp.getId().equals(id))) {
            throw new RuntimeException("New permission already assigned to this role");
        }

        rolePermission.setPermission(newPermission);
        RolePermission updated = rolePermissionRepository.save(rolePermission);
        return toRolePermissionDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRolePermission(Long id) {
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("RolePermission not found"));
        rolePermissionRepository.delete(rolePermission);
    }
}