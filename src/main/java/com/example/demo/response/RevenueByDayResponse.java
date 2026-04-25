package com.example.demo.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
// @AllArgsConstructor sẽ được thay thế bằng constructor tường minh bên dưới
public class RevenueByDayResponse {
    private LocalDate date;
    private BigDecimal totalRevenue;

    public RevenueByDayResponse(LocalDate date, BigDecimal totalRevenue) {
        this.date = date;
        this.totalRevenue = totalRevenue;
    }
}
