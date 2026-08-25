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
        if (userRepository.findByUsername("admin").isEmpty() && userRepository.findByEmail("admin@sirashop.ml").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@sirashop.ml");
            admin.setUsername("admin");
            admin.setFirstName("Super");
            admin.setLastName("Admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Mot de passe haché par défaut
            admin.setRole(Role.SUPER_ADMIN);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("✅ Super Admin par défaut créé : email=admin@sirashop.ml, username=admin, password=admin123");
        }
    }
}
