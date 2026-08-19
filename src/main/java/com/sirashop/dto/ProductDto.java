package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String barcode;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private Long companyId;
    private LocalDateTime createdAt;
}
