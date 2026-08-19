package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatsDto {
    private BigDecimal totalRevenue;
    private BigDecimal salesRevenue;
    private BigDecimal repairRevenue;
    private Long totalSalesCount;
    private Long totalRepairsCount;
    private List<ShopStatsDto> shopStats;
}
