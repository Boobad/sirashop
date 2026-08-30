package com.sirashop.dto;

import com.sirashop.entity.Role;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private Long companyId;
    private Long shopId;
    private Boolean hasSalesEnabled;
    private Boolean hasRepairsEnabled;
    private boolean mustChangePassword;

    // Informations et Alertes d'Abonnement (J-3, Grâce, Expiration)
    private java.time.LocalDate subscriptionExpiresAt;
    private String subscriptionStatus; // ACTIVE, EXPIRING_SOON, GRACE_PERIOD, EXPIRED
    private Long subscriptionDaysRemaining;
    private String subscriptionAlertMessage;
}
