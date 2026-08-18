package com.sirashop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CompanyDto {
    private Long id;
    private String name;
    private boolean isActive;
    private LocalDateTime createdAt;
}
