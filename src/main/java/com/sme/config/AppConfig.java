//package com.sme.config;
//
//import com.sme.entity.CustomUserDetails;
//import com.sme.entity.Permission;
//import com.sme.entity.User;
//import com.sme.security.JwtUtil; // Assuming this is the package
//import com.sme.service.UserService;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//
//@Configuration
//public class AppConfig {
//
//    @Bean
//    public UserDetailsService userDetailsService(UserService userService) {
//        return username -> {
//            User user = userService.getUserEntityByUsername(username);
//            List<Permission> permissions = userService.getUserPermissions(user.getId());
//            return new CustomUserDetails(user, permissions);
//        };
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public JwtUtil jwtUtil() {
//        return new JwtUtil(); // Adjust constructor if it has dependencies
//    }
//}