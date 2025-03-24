package com.sme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.dto.UserDTO;
import com.sme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        UserDTO userDTO = new ObjectMapper().readValue(userJson, UserDTO.class);
        return ResponseEntity.ok(userService.createUser(userDTO, file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/inactive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllInactiveUsers() {
        return ResponseEntity.ok(userService.getAllInactiveUsers());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestPart("user") String userJson,
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
        UserDTO userDTO = new ObjectMapper().readValue(userJson, UserDTO.class);
        return ResponseEntity.ok(userService.updateUser(id, userDTO, file));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws IOException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/restore/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // Principal is UserDetails (e.g., org.springframework.security.core.userdetails.User)
        String email = authentication.getName(); // Email from JWT
        UserDTO userDTO = userService.getCurrentUser(email);
        return ResponseEntity.ok(userDTO);
    }

}