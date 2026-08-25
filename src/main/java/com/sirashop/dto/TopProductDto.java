package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopProductDto {
    private Long productId;
    private String productName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
}
