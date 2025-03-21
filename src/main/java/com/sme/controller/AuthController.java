package com.sme.controller;

import com.sme.dto.AuthRequest;
import com.sme.dto.AuthResponse;
import com.sme.security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        System.out.println("Generated Access Token: " + accessToken);
        System.out.println("Generated Refresh Token: " + refreshToken);


        // Set HTTP-only secure cookies for both tokens
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60) // 15 minutes for access token
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(300) // 7 days for refresh token
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "username", userDetails.getUsername(),
                "roles", userDetails.getAuthorities()
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtUtil.generateToken(userDetails);

        // Set new access token in an HTTP-only cookie
        ResponseCookie newAccessCookie = ResponseCookie.from("newAccessToken", newAccessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60) // 15 minutes
                .build();

        response.addHeader("Set-Cookie", newAccessCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Token refreshed"));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // Set to true in production
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", deleteAccessCookie.toString());
        response.addHeader("Set-Cookie", deleteRefreshCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }@GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(@CookieValue(name = "accessToken", required = false) String accessToken,
                                       @CookieValue(name = "refreshToken", required = false) String refreshToken,
                                       HttpServletResponse response) {
        if (accessToken == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false, "message", "No access token"));
        }

        String username;
        try {
            username = jwtUtil.extractUsername(accessToken);
        } catch (Exception e) {
            // Try refreshing the token if access token is invalid
            if (refreshToken != null && jwtUtil.validateRefreshToken(refreshToken)) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtil.extractUsername(refreshToken));
                String newAccessToken = jwtUtil.generateToken(userDetails);

                ResponseCookie newAccessCookie = ResponseCookie.from("accessToken", newAccessToken)
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(60) // 15 minutes
                        .build();

                response.addHeader("Set-Cookie", newAccessCookie.toString());

                return ResponseEntity.ok(Map.of(
                        "authenticated", true,
                        "username", userDetails.getUsername(),
                        "roles", userDetails.getAuthorities()
                ));
            } else {
                clearInvalidToken(response);
                return ResponseEntity.status(401).body(Map.of("authenticated", false, "message", "Unauthorized"));
            }
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtUtil.validateToken(accessToken, userDetails)) {
            clearInvalidToken(response);
            return ResponseEntity.status(401).body(Map.of("authenticated", false, "message", "Invalid token"));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "username", username,
                "roles", userDetails.getAuthorities()
        ));
    }


    // Helper method to clear invalid token
    private void clearInvalidToken(HttpServletResponse response) {
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", deleteAccessCookie.toString());
    }
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@CookieValue(name = "accessToken", required = false) String accessToken, HttpServletResponse response) {
        if (accessToken == null) {
            clearInvalidToken(response);
            return ResponseEntity.ok(false);
        }

        String username;
        try {
            username = jwtUtil.extractUsername(accessToken);
        } catch (Exception e) {
            clearInvalidToken(response);
            return ResponseEntity.ok(false);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtUtil.validateToken(accessToken, userDetails)) {
            clearInvalidToken(response);
            return ResponseEntity.ok(false);
        }

        return ResponseEntity.ok(true);
    }



}
