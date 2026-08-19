package com.sirashop.dto;

import com.sirashop.entity.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long id;
    private String username;
    private Role role;
    private Long companyId;
    private Long shopId;
}
