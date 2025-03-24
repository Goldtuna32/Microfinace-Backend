package com.sme.service.impl;

import com.sme.config.DefaultPermissionsConfig;
import com.sme.dto.PermissionDTO;
import com.sme.dto.UserDTO;
import com.sme.entity.*;
import com.sme.repository.*;
import com.sme.service.CloudinaryService;
import com.sme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final DefaultPermissionsConfig defaultPermissionsConfig;

    private final RolePermissionRepository rolePermissionRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           BranchRepository branchRepository, CloudinaryService cloudinaryService,
                           PasswordEncoder passwordEncoder, PermissionRepository permissionRepository, DefaultPermissionsConfig defaultPermissionsConfig, RolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
        this.permissionRepository = permissionRepository;
        this.defaultPermissionsConfig = defaultPermissionsConfig;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public UserDTO createUser(UserDTO userDTO, MultipartFile file) {
        if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        user.setDob(userDTO.getDob());

        user.setStatus(1); // Default active
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (file != null && !file.isEmpty()) {
            String imageUrl = cloudinaryService.uploadImage(file);
            user.setProfilePicture(imageUrl);
        } else {
            user.setProfilePicture("default.jpg");
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Branch branch = branchRepository.findById(userDTO.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        user.setRole(role);
        user.setBranch(branch);

        User savedUser = userRepository.save(user);
        assignDefaultPermissions(savedUser, role);
        return mapToDTO(savedUser);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDTO(user);
    }


    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllActiveUsers()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAllInactiveUsers() {
        return userRepository.findAllInactiveUsers()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO, MultipartFile file) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check for conflicts if username or email changes
        if (!user.getUsername().equals(userDTO.getUsername()) && userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (!user.getEmail().equals(userDTO.getEmail()) && userRepository.findByEmail(userDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Set the dob from the userDTO
     user.setDob(userDTO.getDob());

        user.setUpdatedAt(LocalDateTime.now());
        if (file != null && !file.isEmpty()) {
            if (user.getProfilePicture() != null && !user.getProfilePicture().equals("default.jpg")) {
                cloudinaryService.deleteImage(user.getProfilePicture());
            }
            String imageUrl = cloudinaryService.uploadImage(file);
            user.setProfilePicture(imageUrl);
        }

        Role role = roleRepository.findById(userDTO.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        Branch branch = branchRepository.findById(userDTO.getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found"));
        user.setRole(role);
        user.setBranch(branch);

        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    private void assignDefaultPermissions(User user, Role role) {
        List<String> defaultPermissions = defaultPermissionsConfig.getDefaultPermissionsForRole(role.getName());
        for (String perm : defaultPermissions) {
            String[] parts = perm.split("_");
            if (parts.length == 2) {
                Permission permission = permissionRepository.findBypermissionFunctionAndName(parts[0], parts[1])
                        .orElseGet(() -> {
                            Permission newPerm = new Permission();
                            newPerm.setPermissionFunction(parts[0]);
                            newPerm.setName(parts[1]);
                            newPerm.setDescription(parts[1] + " permission for " + parts[0]);
                            newPerm.setCreatedAt(new Date());
                            return permissionRepository.save(newPerm);
                        });

                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(role);
                rolePermission.setPermission(permission);
                rolePermissionRepository.save(rolePermission);
            }
        }
    }

    @Override
    public void deleteUser(Long id) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getProfilePicture() != null && !user.getProfilePicture().equals("default.jpg")) {
            cloudinaryService.deleteImage(user.getProfilePicture());
        }
        userRepository.deleteById(id);
    }

    @Override
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(2);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(1);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User getUserEntityByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public boolean authenticate(String rawPassword, String storedHashedPassword) {
        return passwordEncoder.matches(rawPassword, storedHashedPassword);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());

        // Set the dob from the userDTO
        // Set the dob from the User entity to UserDTO
        if (user.getDob() != null) {
            dto.setDob(user.getDob());  // Map dob from User to UserDTO
        }

        dto.setProfilePicture(user.getProfilePicture());
        dto.setStatus(user.getStatus());
        dto.setRoleId((long) user.getRole().getId());
        dto.setBranchId(user.getBranch().getId());
        return dto;
    }

    @Override
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // UserServiceImpl.java
    @Override
    public UserDTO getCurrentUser(String email) {
        User user = getUserEntityByEmail(email);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRoleId(user.getRole().getId());

        // Add permissions
        List<Permission> permissions = getUserPermissions(user.getId());
        List<PermissionDTO> permissionDTOs = permissions.stream().map(p -> {
            PermissionDTO dto = new PermissionDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setPermissionFunction(p.getPermissionFunction());
            dto.setDescription(p.getDescription());
            return dto;
        }).collect(Collectors.toList());
        userDTO.setPermissions(permissionDTOs);

        return userDTO;
    }

    @Override
    public List<Permission> getUserPermissions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = user.getRole();
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(role.getId());
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .collect(Collectors.toList());
    }

    @Override
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }
}