package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShopStatsDto {
    private Long shopId;
    private String shopName;
    private BigDecimal salesRevenue;
    private Long salesCount;
    private BigDecimal repairRevenue;
    private Long repairsCount;
    private BigDecimal totalRevenue;
}
