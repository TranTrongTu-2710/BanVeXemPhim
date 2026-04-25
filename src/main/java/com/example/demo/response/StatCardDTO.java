package com.example.demo.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatCardDTO {
    private String title;
    private BigDecimal value;
    private String trend; // Ví dụ: "+12.5%"
}
