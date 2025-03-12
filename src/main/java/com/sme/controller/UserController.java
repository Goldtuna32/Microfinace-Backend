package com.sme.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.dto.UserDTO;
import com.sme.entity.User;
import com.sme.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@CrossOrigin("http://localhost:4200")
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> createUser(
            @RequestPart("user") String userJson,  // Accept user data as a raw JSON String
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        // Convert the JSON String to UserDTO
        UserDTO userDTO = new ObjectMapper().readValue(userJson, UserDTO.class);

        // Call the service method to create the user
        return ResponseEntity.ok(userService.createUser(userDTO, file));
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    // Fetch only inactive users (for admin to view deleted users)
    @GetMapping("/inactive")
    public ResponseEntity<List<UserDTO>> getAllInactiveUsers() {
        return ResponseEntity.ok(userService.getAllInactiveUsers());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestPart("user") String userJson, // Accept as JSON String
            @RequestPart(value = "file", required = false) MultipartFile file) throws IOException {

        // Convert JSON string to UserDTO
        UserDTO userDTO = new ObjectMapper().readValue(userJson, UserDTO.class);

        return ResponseEntity.ok(userService.updateUser(id, userDTO, file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    // Soft delete (set status = 2)
    @DeleteMapping("/soft/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Restore (set status = 1)
    @PutMapping("/restore/{id}")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id) {
        userService.restoreUser(id);
        return ResponseEntity.ok().build();
    }
}
