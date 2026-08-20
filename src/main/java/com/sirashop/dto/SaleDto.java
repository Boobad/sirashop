package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SaleDto {
    private Long id;
    private Long companyId;
    private Long shopId;
    private String shopName;
    private Long sellerId;
    private String sellerUsername;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private List<SaleItemDto> items;
    private LocalDateTime createdAt;
}
