package com.sirashop.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SubscriptionPaymentDto {
    private Long id;
    private Long companyId;
    private String companyName;
    private BigDecimal amount;
    private String periodMonth;
    private Integer periodYear;
    private String notes;
    private LocalDateTime paymentDate;
}
