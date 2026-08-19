package com.sirashop.dto;

import com.sirashop.entity.RepairStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RepairTicketDto {
    private Long id;
    private String customerName;
    private String customerPhone;
    private String deviceModel;
    private String issueDescription;
    private BigDecimal estimatedPrice;
    private BigDecimal depositAmount;
    private RepairStatus status;
    private Long companyId;
    private Long shopId;
    private String shopName;
    private Long technicianId;
    private String technicianUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
