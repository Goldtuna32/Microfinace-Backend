package com.sme.service.impl;

import com.sme.dto.UserDTO;
import com.sme.entity.Branch;
import com.sme.entity.Role;
import com.sme.entity.User;
import com.sme.repository.BranchRepository;
import com.sme.repository.RoleRepository;
import com.sme.repository.UserRepository;
import com.sme.service.CloudinaryService;
import com.sme.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final CloudinaryService cloudinaryService;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           BranchRepository branchRepository, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO, MultipartFile file) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setStatus(userDTO.getStatus());
        user.setPassword(userDTO.getPassword());
        user.setDob(new Date());

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            user.setProfilePicture(imageUrl);
        } else {
            user.setProfilePicture("default.jpg");
        }

        // Set role & branch
        Role role = roleRepository.findById(userDTO.getRoleId()).orElseThrow(() -> new RuntimeException("Role not found"));
        Branch branch = branchRepository.findById(userDTO.getBranchId()).orElseThrow(() -> new RuntimeException("Branch not found"));
        user.setRole(role);
        user.setBranch(branch);

        userRepository.save(user);
        return mapToDTO(user);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAllActiveUsers();
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }


    @Override
    public List<UserDTO> getAllInactiveUsers() {
        List<User> users = userRepository.findAllInactiveUsers();
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO, MultipartFile file) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic user details
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setStatus(userDTO.getStatus());

        // Handle image update
        if (file != null && !file.isEmpty()) {
            // Delete the old image if it exists
            if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                cloudinaryService.deleteImage(user.getProfilePicture());
            }

            // Upload new image and update the URL
            String imageUrl = cloudinaryService.uploadImage(file);
            user.setProfilePicture(imageUrl);
        }

        // Update role and branch
        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Branch branch = branchRepository.findById(userDTO.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        user.setRole(role);
        user.setBranch(branch);

        // Save updated user
        userRepository.save(user);

        return mapToDTO(user);
    }


    @Override
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set status to 2 (Inactive)
        user.setStatus(2);

        userRepository.save(user);  // Save updated status
    }
    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Set status back to 1 (Active)
        user.setStatus(1);
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);  // Permanently delete user
    }


    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setStatus(user.getStatus());
        dto.setRoleId((long) user.getRole().getId());
        dto.setBranchId(user.getBranch().getId());
        return dto;
    }
}
