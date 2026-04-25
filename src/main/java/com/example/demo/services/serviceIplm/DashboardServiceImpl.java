package com.example.demo.services.serviceIplm;

import com.example.demo.repository.*;
import com.example.demo.response.*;
import com.example.demo.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final BookingRepository bookingRepository;
    private final FoodOrderItemRepository foodOrderItemRepository;
    private final BookingFoodRepository bookingFoodRepository;
    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;

    @Override
    public DashboardDataResponseDTO getDashboardData(String viewMode) {
        int days = "week".equalsIgnoreCase(viewMode) ? 7 : 30;
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startDateTime = endDateTime.minusDays(days).with(LocalTime.MIN);
        
        LocalDate endDate = endDateTime.toLocalDate();
        LocalDate startDate = startDateTime.toLocalDate();

        // 1. Stat Cards
        BigDecimal totalRevenue = bookingRepository.findTotalRevenueByDateRange(startDateTime, endDateTime);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // SỬA: Dùng countSoldTicketsByDateRange để đếm số ghế
        Long ticketsSold = bookingRepository.countSoldTicketsByDateRange(startDateTime, endDateTime);
        if (ticketsSold == null) ticketsSold = 0L;

        Long newCustomers = userRepository.countNewUsersByDateRange(startDateTime, endDateTime);
        if (newCustomers == null) newCustomers = 0L;

        Long foodFromOrders = foodOrderItemRepository.sumTotalQuantityByDateRange(startDateTime, endDateTime);
        if (foodFromOrders == null) foodFromOrders = 0L;

        Long foodFromBookings = bookingFoodRepository.sumTotalQuantityByDateRange(startDateTime, endDateTime);
        if (foodFromBookings == null) foodFromBookings = 0L;

        Long totalFoodItemsSold = foodFromOrders + foodFromBookings;

        List<StatCardDTO> statCards = new ArrayList<>();
        statCards.add(new StatCardDTO("Doanh Thu", totalRevenue, "+0%"));
        statCards.add(new StatCardDTO("Vé Đã Bán", new BigDecimal(ticketsSold), "+0%"));
        statCards.add(new StatCardDTO("Khách Hàng Mới", new BigDecimal(newCustomers), "+0%"));
        statCards.add(new StatCardDTO("Đồ Ăn Đã Bán", new BigDecimal(totalFoodItemsSold), "+0%"));

        // 2. Revenue Chart Data
        List<Object[]> rawRevenueData = bookingRepository.findRevenueByDayRaw(startDate, endDate);
        List<RevenueByDayResponse> revenueChartData = rawRevenueData.stream()
                .map(record -> {
                    Date sqlDate = (Date) record[0];
                    LocalDate localDate = sqlDate.toLocalDate();
                    BigDecimal revenue = (BigDecimal) record[1];
                    return new RevenueByDayResponse(localDate, revenue);
                })
                .collect(Collectors.toList());

        // 3. Food Sales Chart Data (Top 5)
        List<FoodSaleResponse> foodSalesChartData = dashboardRepository.getTopFoodSales(startDateTime, endDateTime);

        // 4. Recent Bookings
        List<BookingResponseDTO> recentBookings = bookingRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(BookingResponseDTO::new)
                .collect(Collectors.toList());

        return DashboardDataResponseDTO.builder()
                .statCards(statCards)
                .revenueChartData(revenueChartData)
                .foodSalesChartData(foodSalesChartData)
                .recentBookings(recentBookings)
                .build();
    }
}
