package com.sirashop.dto;

import com.sirashop.entity.Role;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String password; // Dans la vraie vie, onv ne le renvoie pas, mais c'est pour la création
    private Role role;
    private Long companyId;
    private Long shopId;
    private boolean isActive;
}
