// PermissionController.java
package com.sme.controller;

import com.sme.dto.UserDTO;
import com.sme.entity.Permission;
import com.sme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin("http://localhost:4200") // For Angular frontend
public class PermissionController {

    @Autowired
    private UserService userService;

    // Get permissions for the current authenticated user
    @GetMapping
    public ResponseEntity<List<Permission>> getCurrentUserPermissions(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // Get username from Authentication
        String username = authentication.getName();

        // Fetch UserDTO using username (assumes UserService has this method)
        UserDTO userDTO = userService.getCurrentUser(username); // Adjust method name if different
        Long userId = userDTO.getId();

        // Fetch permissions using userId
        List<Permission> permissions = userService.getUserPermissions(userId);
        return ResponseEntity.ok(permissions);
    }

    // Optional: Get all permissions in the system (e.g., for admin use)
    @GetMapping("/all")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        List<Permission> permissions = userService.getAllPermissions();
        return ResponseEntity.ok(permissions);
    }
}