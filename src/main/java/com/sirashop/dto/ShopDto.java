package com.sirashop.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ShopDto {
    private Long id;
    private String name;
    private String address;
    private Long companyId; // L'ID de l'entreprise à laquelle appartient la boutique
    private LocalDateTime createdAt;
}
