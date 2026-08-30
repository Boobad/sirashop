package com.sirashop.dto;

import com.sirashop.entity.Role;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String email;
    private String username;
    private String password; // Utilisé à la création ou réinitialisation
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private Long companyId;
    private Long shopId;
    private String shopName;
    private boolean isActive;
    private boolean hasAppAccess = true;
    private boolean mustChangePassword;
}
