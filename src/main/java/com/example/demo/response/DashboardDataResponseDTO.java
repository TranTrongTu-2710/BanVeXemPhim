package com.example.demo.response;

import com.example.demo.model.Booking;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardDataResponseDTO {
    private List<StatCardDTO> statCards;
    private List<RevenueByDayResponse> revenueChartData;
    private List<FoodSaleResponse> foodSalesChartData;
    private List<BookingResponseDTO> recentBookings;
}
