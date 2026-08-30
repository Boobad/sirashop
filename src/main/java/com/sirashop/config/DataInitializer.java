package com.sirashop.config;

import com.sirashop.entity.Role;
import com.sirashop.entity.User;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        userRepository.findByUsername("admin").ifPresentOrElse(
                existingAdmin -> {
                    if (!existingAdmin.isHasAppAccess() || !existingAdmin.isActive()) {
                        existingAdmin.setHasAppAccess(true);
                        existingAdmin.setActive(true);
                        userRepository.save(existingAdmin);
                        System.out.println("🔄 Super Admin 'admin' mis à jour avec accès complet.");
                    }
                },
                () -> {
                    User admin = new User();
                    admin.setEmail("admin@sirashop.ml");
                    admin.setUsername("admin");
                    admin.setFirstName("Super");
                    admin.setLastName("Admin");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(Role.SUPER_ADMIN);
                    admin.setActive(true);
                    admin.setHasAppAccess(true);
                    admin.setMustChangePassword(false);
                    userRepository.save(admin);
                    System.out.println("✅ Super Admin par défaut créé : email=admin@sirashop.ml, username=admin, password=admin123");
                }
        );

        // Mise à jour de sécurité pour tous les Super Admins et Propriétaires existants
        userRepository.findAll().forEach(u -> {
            if ((u.getRole() == Role.SUPER_ADMIN || u.getRole() == Role.COMPANY_OWNER) && !u.isHasAppAccess()) {
                u.setHasAppAccess(true);
                userRepository.save(u);
            }
        });
    }
}
