package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentMethodStatsDto {
    private String paymentMethod;
    private Long count;
    private BigDecimal totalAmount;
    private Double percentage;
}
