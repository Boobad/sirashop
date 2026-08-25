package com.sirashop.service;

import com.sirashop.dto.SuperAdminStatsDto;
import com.sirashop.dto.UserDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Role;
import com.sirashop.entity.SubscriptionPayment;
import com.sirashop.entity.User;
import com.sirashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final SubscriptionPaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public SuperAdminStatsDto getStats() {
        List<Company> companies = companyRepository.findAll();
        long total = companies.size();
        long active = companies.stream().filter(Company::isActive).count();
        long blocked = total - active;

        BigDecimal tariff = new BigDecimal("30000"); // 30 000 FCFA / mois / entreprise
        BigDecimal expectedMonthlyRevenue = tariff.multiply(BigDecimal.valueOf(total));

        List<SubscriptionPayment> allPayments = paymentRepository.findAll();
        BigDecimal totalSubscriptionRevenue = allPayments.stream()
                .map(SubscriptionPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int currentYear = LocalDate.now().getYear();
        BigDecimal currentMonthSubscriptionRevenue = allPayments.stream()
                .filter(p -> p.getPeriodYear() != null && p.getPeriodYear() == currentYear)
                .map(SubscriptionPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SuperAdminStatsDto dto = new SuperAdminStatsDto();
        dto.setTotalCompanies(total);
        dto.setActiveCompanies(active);
        dto.setBlockedCompanies(blocked);
        dto.setTotalShops(shopRepository.count());
        dto.setTotalUsers(userRepository.count());
        dto.setMonthlyTariff(tariff);
        dto.setExpectedMonthlyRevenue(expectedMonthlyRevenue);
        dto.setTotalSubscriptionRevenue(totalSubscriptionRevenue);
        dto.setCurrentMonthSubscriptionRevenue(currentMonthSubscriptionRevenue);
        return dto;
    }

    public UserDto createSuperAdmin(UserDto dto) {
        User admin = new User();
        String email = dto.getEmail() != null ? dto.getEmail() : dto.getUsername();
        admin.setEmail(email);
        admin.setUsername(dto.getUsername() != null ? dto.getUsername() : email);
        admin.setPassword(passwordEncoder.encode(dto.getPassword()));
        admin.setFirstName(dto.getFirstName() != null ? dto.getFirstName() : "Super");
        admin.setLastName(dto.getLastName() != null ? dto.getLastName() : "Admin");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setActive(true);

        User saved = userRepository.save(admin);

        // Envoi de l'email avec les identifiants au Super Admin
        emailService.sendAccountCreatedEmailAsync(
                dto.getUsername(),
                dto.getUsername(),
                dto.getPassword(),
                "Super Administrateur",
                "Plateforme SiraShop"
        );

        UserDto result = new UserDto();
        result.setId(saved.getId());
        result.setUsername(saved.getUsername());
        result.setRole(saved.getRole());
        result.setActive(saved.isActive());
        return result;
    }

    public List<UserDto> getSuperAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.SUPER_ADMIN)
                .map(u -> {
                    UserDto dto = new UserDto();
                    dto.setId(u.getId());
                    dto.setUsername(u.getUsername());
                    dto.setRole(u.getRole());
                    dto.setActive(u.isActive());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
