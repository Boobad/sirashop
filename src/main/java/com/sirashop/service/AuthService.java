package com.sirashop.service;

import com.sirashop.dto.LoginRequest;
import com.sirashop.dto.LoginResponse;
import com.sirashop.entity.Company;
import com.sirashop.entity.Role;
import com.sirashop.entity.User;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LoginResponse login(LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier == null) {
            throw new RuntimeException("L'adresse email ou l'identifiant est obligatoire");
        }

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByUsername(identifier))
                .orElseThrow(() -> new RuntimeException("Identifiant ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Identifiant ou mot de passe incorrect");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Votre compte personnel est désactivé. Veuillez contacter le support.");
        }

        if (user.getRole() != Role.SUPER_ADMIN && user.getRole() != Role.COMPANY_OWNER && !user.isHasAppAccess()) {
            throw new RuntimeException("Ce compte est enregistré comme technicien/employé sans accès à l'application.");
        }

        // Vérifications de l'entreprise associée (Abonnement, Expiration + 3 jours de grâce)
        if (user.getRole() != Role.SUPER_ADMIN && user.getCompany() != null) {
            Company company = user.getCompany();

            if (company.getSubscriptionExpiresAt() != null) {
                LocalDate now = LocalDate.now();
                LocalDate expiresAt = company.getSubscriptionExpiresAt();
                LocalDate gracePeriodEnd = expiresAt.plusDays(3);

                // Si la date d'expiration + 3 jours de grâce est dépassée -> Suspension automatique
                if (now.isAfter(gracePeriodEnd)) {
                    company.setActive(false);
                    companyRepository.save(company);
                    throw new RuntimeException("⚠️ Accès suspendu : L'abonnement de votre entreprise a expiré depuis plus de 3 jours (le " 
                            + expiresAt.format(DATE_FORMATTER) 
                            + "). Vos accès ont été automatiquement bloqués. Veuillez régulariser votre abonnement.");
                }
            }

            if (!company.isActive()) {
                throw new RuntimeException("⚠️ Accès suspendu : L'entreprise '" + company.getName() + "' est actuellement désactivée. Veuillez régulariser votre abonnement.");
            }
        }

        String token = "JWT_" + UUID.randomUUID().toString().replace("-", "") + "_" + user.getId();

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(user.getUsername() != null ? user.getUsername() : user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setMustChangePassword(user.isMustChangePassword());

        if (user.getCompany() != null) {
            Company company = user.getCompany();
            response.setCompanyId(company.getId());
            response.setHasSalesEnabled(company.isHasSalesEnabled());
            response.setHasRepairsEnabled(company.isHasRepairsEnabled());

            // Calcul du statut de l'abonnement et des alertes (J-3 et Grâce)
            if (company.getSubscriptionExpiresAt() != null) {
                LocalDate now = LocalDate.now();
                LocalDate expiresAt = company.getSubscriptionExpiresAt();
                long daysRemaining = ChronoUnit.DAYS.between(now, expiresAt);
                response.setSubscriptionExpiresAt(expiresAt);
                response.setSubscriptionDaysRemaining(daysRemaining);

                String formattedDate = expiresAt.format(DATE_FORMATTER);

                if (now.isAfter(expiresAt)) {
                    // Période de grâce (J+1 à J+3)
                    long graceDaysLeft = ChronoUnit.DAYS.between(now, expiresAt.plusDays(3));
                    response.setSubscriptionStatus("GRACE_PERIOD");
                    response.setSubscriptionAlertMessage("🚨 Période de grâce : Votre abonnement a expiré le " + formattedDate 
                            + ". Il vous reste " + Math.max(0, graceDaysLeft) + " jour(s) avant la coupure totale de vos accès.");
                } else if (daysRemaining <= 3) {
                    // Alerte J-3 (Expiration imminente)
                    response.setSubscriptionStatus("EXPIRING_SOON");
                    if (daysRemaining == 0) {
                        response.setSubscriptionAlertMessage("⚠️ Attention : Votre abonnement expire aujourd'hui (" + formattedDate + "). Pensez à le renouveler.");
                    } else {
                        response.setSubscriptionAlertMessage("⚠️ Attention : Votre abonnement expire dans " + daysRemaining + " jour(s) (le " + formattedDate + "). Pensez à le renouveler.");
                    }
                } else {
                    response.setSubscriptionStatus("ACTIVE");
                }
            } else {
                response.setSubscriptionStatus("ACTIVE");
            }
        } else if (user.getRole() == Role.SUPER_ADMIN) {
            response.setHasSalesEnabled(true);
            response.setHasRepairsEnabled(true);
            response.setSubscriptionStatus("ACTIVE");
        }

        if (user.getShop() != null) {
            response.setShopId(user.getShop().getId());
        }

        return response;
    }
}
