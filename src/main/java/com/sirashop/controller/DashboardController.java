package com.sirashop.controller;

import com.sirashop.dto.StatsDto;
import com.sirashop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/company/{companyId}")
    public ResponseEntity<StatsDto> getCompanyStats(@PathVariable Long companyId) {
        return ResponseEntity.ok(dashboardService.getCompanyStats(companyId));
    }
}
