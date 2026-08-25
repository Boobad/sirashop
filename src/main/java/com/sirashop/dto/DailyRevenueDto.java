package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyRevenueDto {
    private LocalDate date;
    private BigDecimal salesRevenue;
    private BigDecimal repairRevenue;
    private BigDecimal totalRevenue;
    private Long salesCount;
    private Long repairsCount;
}
