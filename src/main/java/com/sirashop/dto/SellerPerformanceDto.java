package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SellerPerformanceDto {
    private Long sellerId;
    private String sellerName;
    private Long totalSalesCount;
    private BigDecimal totalRevenue;
}
