package com.sirashop.dto;

import lombok.Data;

@Data
public class CompanyRegistrationDto {
    private String companyName;
    private String phone;
    private String ownerName;
    private String ownerEmail;
    private String ownerUsername;
    private String ownerPassword;
    private boolean hasSalesEnabled = true;
    private boolean hasRepairsEnabled;
}
