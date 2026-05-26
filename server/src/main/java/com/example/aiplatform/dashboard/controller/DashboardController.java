package com.example.aiplatform.dashboard.controller;

import com.example.aiplatform.auth.security.AuthenticatedUser;
import com.example.aiplatform.dashboard.dto.DashboardSummaryResponse;
import com.example.aiplatform.dashboard.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(@AuthenticationPrincipal AuthenticatedUser user) {
        return dashboardService.getSummary(user.id());
    }
}
