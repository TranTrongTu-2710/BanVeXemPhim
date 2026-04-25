package com.example.demo.controller;

import com.example.demo.response.DashboardDataResponseDTO;
import com.example.demo.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDataResponseDTO> getDashboardData(
            @RequestParam(defaultValue = "week") String viewMode) {
        return ResponseEntity.ok(dashboardService.getDashboardData(viewMode));
    }
}
