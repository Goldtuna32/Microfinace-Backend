package com.sme.controller;

import com.sme.dto.BranchDashboardDTO;
import com.sme.dto.BranchStatsDTO;
import com.sme.dto.UserDTO;
import com.sme.security.JwtUtil;
import com.sme.service.BranchDashboardService;
import com.sme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branch-dashboard")
@RequiredArgsConstructor
public class BranchDashboardController {

    private final BranchDashboardService dashboardService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<BranchDashboardDTO> getBranchDashboardStats(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            // Get current user with branch information
            UserDTO currentUser = userService.getCurrentUser(authentication.getName());

            if (currentUser.getBranchId() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            return ResponseEntity.ok(
                    dashboardService.getBranchDashboardStats(currentUser.getBranchId())
            );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BranchStatsDTO>> getAllBranchesStats() {
        return ResponseEntity.ok(dashboardService.getAllBranchesStats());
    }
}