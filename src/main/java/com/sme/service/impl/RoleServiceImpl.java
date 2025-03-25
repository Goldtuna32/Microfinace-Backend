package com.sme.service.impl;

import com.sme.dto.PermissionDTO;
import com.sme.dto.RoleDTO;
import com.sme.entity.Permission;
import com.sme.entity.Role;
import com.sme.entity.RolePermission;
import com.sme.repository.PermissionRepository;
import com.sme.repository.RolePermissionRepository;
import com.sme.repository.RoleRepository;
import com.sme.service.RoleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

// RoleServiceImpl.java
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.findByName(roleDTO.getName()).isPresent()) {
            throw new RuntimeException("Role name already exists");
        }

        Role role = new Role();
        role.setName(roleDTO.getName());
        role.setDescription(roleDTO.getDescription());
        role = roleRepository.save(role);

        if (roleDTO.getPermissions() != null && !roleDTO.getPermissions().isEmpty()) {
            for (PermissionDTO permDTO : roleDTO.getPermissions()) {
                Permission permission = permissionRepository.findByPermissionFunctionAndName(
                                permDTO.getPermissionFunction(), permDTO.getName())
                        .orElseGet(() -> {
                            Permission newPerm = new Permission();
                            newPerm.setPermissionFunction(permDTO.getPermissionFunction());
                            newPerm.setName(permDTO.getName());
                            newPerm.setDescription(permDTO.getDescription());
                            newPerm.setCreatedAt(new Date());
                            return permissionRepository.save(newPerm);
                        });

                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                rolePermissionRepository.save(rolePermission);
            }
        }

        return mapToDTO(role);
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO mapToDTO(Role role) {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(role.getId());
        roleDTO.setName(role.getName());
        roleDTO.setDescription(role.getDescription());

        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());
        List<PermissionDTO> permissions = rolePermissions.stream()
                .map(rp -> {
                    Permission perm = rp.getPermission();
                    PermissionDTO permDTO = new PermissionDTO();
                    permDTO.setId(perm.getId());
                    permDTO.setPermissionFunction(perm.getPermissionFunction());
                    permDTO.setName(perm.getName());
                    permDTO.setDescription(perm.getDescription());
                    return permDTO;
                })
                .collect(Collectors.toList());
        roleDTO.setPermissions(permissions);

        return roleDTO;
    }
}