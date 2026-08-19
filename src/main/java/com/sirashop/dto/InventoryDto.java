package com.sirashop.dto;

import lombok.Data;

@Data
public class InventoryDto {
    private Long id;
    private Long productId;
    private String productName;
    private Long shopId;
    private String shopName;
    private Integer quantity;
    private Integer alertThreshold;
}
