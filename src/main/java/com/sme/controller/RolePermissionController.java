package com.sme.controller;

import com.sme.dto.RolePermissionDTO;
import com.sme.entity.RolePermission;
import com.sme.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/role-permissions")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @Autowired
    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CREATE')")
    public ResponseEntity<RolePermissionDTO> createRolePermission(
            @RequestParam Long roleId,
            @RequestParam Long permissionId) {
        RolePermissionDTO rolePermission = rolePermissionService.createRolePermission(roleId, permissionId);
        return ResponseEntity.ok(rolePermission);
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_READ')")
    public ResponseEntity<List<RolePermissionDTO>> getAllRolePermissions() {
        return ResponseEntity.ok(rolePermissionService.getAllRolePermissions());
    }

    @GetMapping("/role/{roleId}")
    @PreAuthorize("hasRole('ROLE_READ')")
    public ResponseEntity<List<RolePermissionDTO>> getRolePermissionsByRoleId(@PathVariable Long roleId) {
        return ResponseEntity.ok(rolePermissionService.getRolePermissionsByRoleId(roleId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_UPDATE')")
    public ResponseEntity<RolePermissionDTO> updateRolePermission(
            @PathVariable Long id,
            @RequestParam Long permissionId) {
        RolePermissionDTO updatedRolePermission = rolePermissionService.updateRolePermission(id, permissionId);
        return ResponseEntity.ok(updatedRolePermission);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_DELETE')")
    public ResponseEntity<Void> deleteRolePermission(@PathVariable Long id) {
        rolePermissionService.deleteRolePermission(id);
        return ResponseEntity.noContent().build();
    }
}