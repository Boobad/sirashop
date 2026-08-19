package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SaleItemDto {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
