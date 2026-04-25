package com.example.demo.services;

import com.example.demo.response.DashboardDataResponseDTO;

public interface DashboardService {
    DashboardDataResponseDTO getDashboardData(String viewMode);
}
