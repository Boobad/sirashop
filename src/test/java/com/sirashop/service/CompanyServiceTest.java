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
    @DisplayName("Devrait créer une entreprise avec son propriétaire, téléphone 8+ chiffres et ses modules activés")
    void createCompanyWithOwner_Success() {
        CompanyRegistrationDto dto = new CompanyRegistrationDto();
        dto.setCompanyName("Boutique High-Tech & SAV");
        dto.setPhone("66000000"); // 8 chiffres
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
    @DisplayName("Devrait autoriser plusieurs entreprises avec le même nom (ex: lol ou Boutique Moderne) tant que l'email du propriétaire est unique")
    void createCompanyWithOwner_SameCompanyNameDifferentOwner_Success() {
        CompanyRegistrationDto dto1 = new CompanyRegistrationDto();
        dto1.setCompanyName("lol");
        dto1.setOwnerEmail("owner1@sirashop.ml");
        dto1.setPhone("76000001");

        Company company1 = new Company();
        company1.setId(10L);
        company1.setName("lol");

        when(userRepository.existsByEmailIgnoreCase("owner1@sirashop.ml")).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(company1);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_pwd");

        CompanyDto created = companyService.createCompanyWithOwner(dto1);
        assertNotNull(created);
        assertEquals("lol", created.getName());
    }

    @Test
    @DisplayName("Devrait rejeter la création si le téléphone comporte moins de 8 chiffres")
    void createCompanyWithOwner_PhoneTooShort_ThrowsException() {
        CompanyRegistrationDto dto = new CompanyRegistrationDto();
        dto.setCompanyName("Boutique High-Tech");
        dto.setOwnerEmail("bakary@sirashop.ml");
        dto.setPhone("12345"); // 5 chiffres (trop court)

        when(userRepository.existsByEmailIgnoreCase("bakary@sirashop.ml")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> companyService.createCompanyWithOwner(dto));
        assertTrue(ex.getMessage().contains("au moins 8 chiffres"));
    }

    @Test
    @DisplayName("Devrait rejeter la création si l'email du propriétaire est déjà utilisé")
    void createCompanyWithOwner_DuplicateEmail_ThrowsException() {
        CompanyRegistrationDto dto = new CompanyRegistrationDto();
        dto.setCompanyName("Boutique High-Tech");
        dto.setOwnerEmail("bakary@sirashop.ml");
        dto.setPhone("76000000");

        when(userRepository.existsByEmailIgnoreCase("bakary@sirashop.ml")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> companyService.createCompanyWithOwner(dto));
        assertTrue(ex.getMessage().contains("est déjà utilisée"));
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
