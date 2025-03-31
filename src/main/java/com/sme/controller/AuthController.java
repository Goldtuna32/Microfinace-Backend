package com.sme.controller;

import com.sme.dto.AuthRequest;
import com.sme.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${spring.security.user.email}")
    private String superAdminUsername;

    @Value("${spring.security.user.password}")
    private String superAdminPassword;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication;

            // Check for super admin credentials
            if (loginRequest.getEmail().equals(superAdminUsername) &&
                    loginRequest.getPassword().equals(superAdminPassword)) {
                // Super admin login
                authentication = new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword(),
                        List.of(() -> "ROLE_SUPER_ADMIN") // Set super admin role
                );
            } else {
                // Normal user login
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(),
                                loginRequest.getPassword()
                        )
                );
            }

            // Extract roles from authorities
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                    .collect(Collectors.toList());

            // Generate JWT tokens
            String accessToken = jwtUtil.generateAccessToken(loginRequest.getEmail(), roles);
            String refreshToken = jwtUtil.generateRefreshToken(loginRequest.getEmail(),roles);

            // Secure refresh token cookie
            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false) // Use true for HTTPS
                    .path("/")
                    .maxAge(3 * 60) // 7 days
                    .sameSite("Strict")
                    .domain("localhost") // Update this in production
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
            return ResponseEntity.ok().body(Map.of("accessToken", accessToken));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No cookies found");
        }
        String refreshToken = Arrays.stream(cookies)
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            String email = jwtUtil.getEmailFromToken(refreshToken);
            List<String> roles = jwtUtil.extractRolesFromToken(refreshToken); // Extract roles
            String newAccessToken = jwtUtil.generateAccessToken(email, roles);
            return ResponseEntity.ok().body(Map.of("accessToken", newAccessToken));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Expire the refresh token cookie
        ResponseCookie expiredCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
        return ResponseEntity.ok().build();
    }
}
