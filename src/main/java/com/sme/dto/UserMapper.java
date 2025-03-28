package com.sme.dto;

import com.sme.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setDob(user.getDob());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setStatus(user.getStatus());
        dto.setLastLogin(user.getLastLogin());

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
        }

        if (user.getBranch() != null) {
            dto.setBranchId(user.getBranch().getId());
        }

        return dto;
    }

    public User toEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setDob(dto.getDob());
        user.setProfilePicture(dto.getProfilePicture());
        user.setStatus(dto.getStatus());

        // Relationships would be set by ID in service layer
        return user;
    }
}