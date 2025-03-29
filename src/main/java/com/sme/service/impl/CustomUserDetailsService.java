package com.sme.service.impl;

import com.sme.entity.User;
import com.sme.repository.UserRepository;
import org.springframework.boot.info.GitProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails; // Correct import
import org.springframework.security.core.userdetails.UserDetailsService; // Correct import
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Get role name from the database (preserve original case)
        String roleName = (user.getRole() != null)
                ? user.getRole().getName()
                : "USER"; // Default role

        // Add ROLE_ prefix if missing (do NOT uppercase)
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        GrantedAuthority authority = new SimpleGrantedAuthority(roleName);
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }


}
