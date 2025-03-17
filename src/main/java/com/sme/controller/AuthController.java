package com.sme.controller;

import com.sme.dto.AuthRequest;
import com.sme.dto.AuthResponse;
import com.sme.security.JwtUtil;
import jakarta.servlet.http.Cookie;
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
@CrossOrigin("http://localhost:4200")
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
        // Set access token as an HTTP-only secure cookie
        // Create HTTP-only cookie for refresh token
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)  // Prevents JavaScript access (XSS protection)
                .secure(false)    // Ensures it's sent only over HTTPS
                .path("/")       // Available to the whole site
                .maxAge( 120*1000) // 7 days
                .sameSite("Lax") // Prevents CSRF attacks
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());

        // Send access token in response
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        // Extract username from refresh token
        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Generate new access token
        String newAccessToken = jwtUtil.generateToken(userDetails);


        // Optionally, you may want to issue a new refresh token and update the cookie
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
        ResponseCookie newRefreshCookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(300*1000) // 7 days
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", newRefreshCookie.toString());

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Create a cookie with the same name as the refresh token
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // Ensure the cookie is only sent over HTTPS
        refreshCookie.setPath("/"); // Match the path of the original cookie
        refreshCookie.setMaxAge(0); // Set the cookie to expire immediately

        // Add the cookie to the response to overwrite and invalidate the existing cookie
        response.addCookie(refreshCookie);

        return ResponseEntity.ok("Logged out successfully.");
    }
}
