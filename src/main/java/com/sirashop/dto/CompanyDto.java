package com.sirashop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanyDto {
    private Long id;
    private String name;
    private String phone;
    private String ownerName;
    private boolean hasSalesEnabled = true;
    private boolean hasRepairsEnabled;
    private boolean isActive;
    private LocalDateTime createdAt;
}
