package com.sirashop.service;

import com.sirashop.dto.LoginRequest;
import com.sirashop.dto.LoginResponse;
import com.sirashop.entity.Company;
import com.sirashop.entity.Role;
import com.sirashop.entity.User;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User owner;
    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setName("Sira High-Tech");
        company.setActive(true);

        owner = new User();
        owner.setId(10L);
        owner.setEmail("owner@sirashop.ml");
        owner.setUsername("owner@sirashop.ml");
        owner.setPassword("encoded_pwd");
        owner.setRole(Role.COMPANY_OWNER);
        owner.setCompany(company);
        owner.setActive(true);
        owner.setHasAppAccess(true);
    }

    @Test
    @DisplayName("Devrait renvoyer une alerte J-3 (EXPIRING_SOON) quand l'abonnement expire dans 2 jours")
    void login_ExpiringSoon_ReturnsWarningAlert() {
        company.setSubscriptionExpiresAt(LocalDate.now().plusDays(2)); // Expire dans 2 jours

        LoginRequest request = new LoginRequest();
        request.setEmail("owner@sirashop.ml");
        request.setPassword("pass123");

        when(userRepository.findByEmail("owner@sirashop.ml")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("pass123", "encoded_pwd")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("EXPIRING_SOON", response.getSubscriptionStatus());
        assertEquals(2L, response.getSubscriptionDaysRemaining());
        assertNotNull(response.getSubscriptionAlertMessage());
        assertTrue(response.getSubscriptionAlertMessage().contains("expire dans 2 jour(s)"));
    }

    @Test
    @DisplayName("Devrait renvoyer une alerte urgente (GRACE_PERIOD) quand l'abonnement a expiré mais qu'on est dans les 3 jours de grâce")
    void login_GracePeriod_ReturnsUrgentAlert() {
        company.setSubscriptionExpiresAt(LocalDate.now().minusDays(1)); // Expiré hier (en période de grâce)

        LoginRequest request = new LoginRequest();
        request.setEmail("owner@sirashop.ml");
        request.setPassword("pass123");

        when(userRepository.findByEmail("owner@sirashop.ml")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("pass123", "encoded_pwd")).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("GRACE_PERIOD", response.getSubscriptionStatus());
        assertNotNull(response.getSubscriptionAlertMessage());
        assertTrue(response.getSubscriptionAlertMessage().contains("Période de grâce"));
    }

    @Test
    @DisplayName("Devrait bloquer la connexion et suspendre l'entreprise quand l'abonnement a expiré depuis plus de 3 jours")
    void login_BeyondGracePeriod_BlocksLoginAndSuspendsCompany() {
        company.setSubscriptionExpiresAt(LocalDate.now().minusDays(4)); // Expiré il y a 4 jours (> 3 jours grâce)

        LoginRequest request = new LoginRequest();
        request.setEmail("owner@sirashop.ml");
        request.setPassword("pass123");

        when(userRepository.findByEmail("owner@sirashop.ml")).thenReturn(Optional.of(owner));
        when(passwordEncoder.matches("pass123", "encoded_pwd")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertTrue(ex.getMessage().contains("expiré depuis plus de 3 jours"));
        assertFalse(company.isActive(), "L'entreprise doit être suspendue automatiquement");
        verify(companyRepository).save(company);
    }
}
