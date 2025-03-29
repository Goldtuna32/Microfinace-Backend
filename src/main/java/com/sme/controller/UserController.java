package com.sme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.dto.UserDTO;
import com.sme.entity.Permission;
import com.sme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER_CREATE')")
    public ResponseEntity<UserDTO> createUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        UserDTO userDTO = new ObjectMapper().readValue(userJson, UserDTO.class);
        return ResponseEntity.ok(userService.createUser(userDTO, file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER_READ')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER_READ')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('USER_READ')")
    public ResponseEntity<List<UserDTO>> getAllInactiveUsers() {
        return ResponseEntity.ok(userService.getAllInactiveUsers());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER_UPDATE')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        UserDTO userDTO = new ObjectMapper().readValue(userJson, UserDTO.class);
        return ResponseEntity.ok(userService.updateUser(id, userDTO, file));
    }


    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasRole('USER_DELETE')")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/restore/{id}")
    @PreAuthorize("hasRole('USER_DELETE')")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UserDTO userDTO = userService.getCurrentUser(authentication.getName());
            return ResponseEntity.ok(userDTO);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Add to UserController.java
    @GetMapping("/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(userService.getAllPermissions());
    }

}