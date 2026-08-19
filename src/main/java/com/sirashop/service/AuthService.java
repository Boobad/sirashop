package com.sirashop.service;

import com.sirashop.dto.LoginRequest;
import com.sirashop.dto.LoginResponse;
import com.sirashop.entity.Role;
import com.sirashop.entity.User;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Identifiant ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Identifiant ou mot de passe incorrect");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Votre compte personnel est désactivé. Veuillez contacter le support.");
        }

        // Vérifier si l'entreprise associée est suspendue (Abonnement non payé)
        if (user.getRole() != Role.SUPER_ADMIN && user.getCompany() != null && !user.getCompany().isActive()) {
            throw new RuntimeException("⚠️ Accès suspendu : L'entreprise '" + user.getCompany().getName() + "' est actuellement désactivée. Veuillez régulariser votre abonnement.");
        }

        String token = "JWT_" + UUID.randomUUID().toString().replace("-", "") + "_" + user.getId();

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());

        if (user.getCompany() != null) {
            response.setCompanyId(user.getCompany().getId());
        }
        if (user.getShop() != null) {
            response.setShopId(user.getShop().getId());
        }

        return response;
    }
}
