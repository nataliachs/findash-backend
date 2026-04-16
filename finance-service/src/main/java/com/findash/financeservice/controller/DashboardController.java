package com.findash.financeservice.controller;

import com.findash.financeservice.dto.DashboardSummaryResponse;
import com.findash.financeservice.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:4200", "http://findash-frontend-001.s3-website-ap-southeast-1.amazonaws.com"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(Authentication authentication) {
        String ownerEmail = authentication.getName();
        return ResponseEntity.ok(dashboardService.getSummary(ownerEmail));
    }
}