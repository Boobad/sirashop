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
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Mot de passe haché par défaut
            admin.setRole(Role.SUPER_ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("✅ Super Admin par défaut créé : username=admin, password=admin123");
        }
    }
}
