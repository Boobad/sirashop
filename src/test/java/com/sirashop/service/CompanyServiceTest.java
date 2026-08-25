package com.sirashop.service;

import com.sirashop.dto.CompanyDto;
import com.sirashop.dto.CompanyRegistrationDto;
import com.sirashop.entity.Company;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CompanyService companyService;

    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setName("Quincaillerie Moderne");
        company.setPhone("76000000");
        company.setOwnerName("Moussa Diarra");
        company.setHasSalesEnabled(true);
        company.setHasRepairsEnabled(false);
        company.setActive(true);
    }

    @Test
    @DisplayName("Devrait créer une entreprise avec ses modules configurés")
    void createCompany_Success() {
        CompanyDto dto = new CompanyDto();
        dto.setName("Quincaillerie Moderne");
        dto.setPhone("76000000");
        dto.setOwnerName("Moussa Diarra");
        dto.setHasSalesEnabled(true);
        dto.setHasRepairsEnabled(false); // Quincaillerie : Pas de SAV

        when(companyRepository.save(any(Company.class))).thenReturn(company);

        CompanyDto created = companyService.createCompany(dto);

        assertNotNull(created);
        assertEquals("Quincaillerie Moderne", created.getName());
        assertTrue(created.isHasSalesEnabled());
        assertFalse(created.isHasRepairsEnabled());
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    @DisplayName("Devrait créer une entreprise avec son propriétaire et ses modules activés")
    void createCompanyWithOwner_Success() {
        CompanyRegistrationDto dto = new CompanyRegistrationDto();
        dto.setCompanyName("Boutique High-Tech & SAV");
        dto.setPhone("66000000");
        dto.setOwnerName("Bakary Diallo");
        dto.setOwnerEmail("bakary@sirashop.ml");
        dto.setOwnerPassword("pass123");
        dto.setHasSalesEnabled(true);
        dto.setHasRepairsEnabled(true); // Vente + SAV

        Company techCompany = new Company();
        techCompany.setId(2L);
        techCompany.setName("Boutique High-Tech & SAV");
        techCompany.setHasSalesEnabled(true);
        techCompany.setHasRepairsEnabled(true);

        when(userRepository.existsByEmailIgnoreCase("bakary@sirashop.ml")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(techCompany);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass");

        CompanyDto created = companyService.createCompanyWithOwner(dto);

        assertNotNull(created);
        assertTrue(created.isHasSalesEnabled());
        assertTrue(created.isHasRepairsEnabled());

        verify(userRepository).save(any(User.class));
        verify(emailService).sendAccountCreatedEmailAsync(
                eq("bakary@sirashop.ml"),
                eq("Bakary Diallo"),
                eq("pass123"),
                eq("Propriétaire d'entreprise"),
                eq("Boutique High-Tech & SAV")
        );
    }

    @Test
    @DisplayName("Devrait créer une entreprise avec le mot de passe par défaut si le propriétaire n'a pas saisi de mot de passe")
    void createCompanyWithOwner_DefaultPassword_Success() {
        CompanyRegistrationDto dto = new CompanyRegistrationDto();
        dto.setCompanyName("Boutique Mode & Style");
        dto.setOwnerName("Fatoumata Coulibaly");
        dto.setOwnerEmail("fatou@sirashop.ml");
        dto.setOwnerPassword(null); // Pas de mot de passe
        dto.setHasSalesEnabled(true);

        Company fashionCompany = new Company();
        fashionCompany.setId(3L);
        fashionCompany.setName("Boutique Mode & Style");

        when(userRepository.existsByEmailIgnoreCase("fatou@sirashop.ml")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(fashionCompany);
        when(passwordEncoder.encode(UserService.DEFAULT_PASSWORD)).thenReturn("encoded_default_pass");

        CompanyDto created = companyService.createCompanyWithOwner(dto);

        assertNotNull(created);
        verify(userRepository).save(any(User.class));
        verify(emailService).sendAccountCreatedEmailAsync(
                eq("fatou@sirashop.ml"),
                eq("Fatoumata Coulibaly"),
                eq(UserService.DEFAULT_PASSWORD),
                eq("Propriétaire d'entreprise"),
                eq("Boutique Mode & Style")
        );
    }

    @Test
    @DisplayName("Devrait mettre à jour la configuration des modules d'une entreprise")
    void updateCompany_Success() {
        CompanyDto updateDto = new CompanyDto();
        updateDto.setName("Quincaillerie & SAV");
        updateDto.setHasSalesEnabled(true);
        updateDto.setHasRepairsEnabled(true); // Activation du module SAV

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyDto result = companyService.updateCompany(1L, updateDto);

        assertEquals("Quincaillerie & SAV", result.getName());
        assertTrue(result.isHasRepairsEnabled(), "Le module SAV doit être activé");
        verify(companyRepository).save(company);
    }
}
