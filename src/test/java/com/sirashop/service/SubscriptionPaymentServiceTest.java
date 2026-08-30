package com.sirashop.service;

import com.sirashop.dto.SubscriptionPaymentDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.SubscriptionPayment;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.SubscriptionPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPaymentServiceTest {

    @Mock
    private SubscriptionPaymentRepository paymentRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private SubscriptionPaymentService paymentService;

    private Company company;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setName("Boutique Test");
        company.setActive(false);
    }

    @Test
    @DisplayName("Devrait enregistrer le paiement et activer l'entreprise avec succès si pas encore payé")
    void recordPayment_Success() {
        SubscriptionPaymentDto dto = new SubscriptionPaymentDto();
        dto.setCompanyId(1L);
        dto.setAmount(new BigDecimal("30000"));
        dto.setPeriodMonth("Août");
        dto.setPeriodYear(2026);
        dto.setPaymentMethod("ORANGE_MONEY");
        dto.setNotes("Paiement via Orange Money");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentRepository.existsByCompanyIdAndPeriodMonthIgnoreCaseAndPeriodYear(1L, "Août", 2026))
                .thenReturn(false);

        SubscriptionPayment savedPayment = new SubscriptionPayment();
        savedPayment.setId(10L);
        savedPayment.setCompany(company);
        savedPayment.setAmount(dto.getAmount());
        savedPayment.setPeriodMonth("Août");
        savedPayment.setPeriodYear(2026);
        savedPayment.setPaymentMethod("ORANGE_MONEY");
        savedPayment.setNotes(dto.getNotes());
        savedPayment.setPaymentDate(LocalDateTime.now());

        when(paymentRepository.save(any(SubscriptionPayment.class))).thenReturn(savedPayment);

        SubscriptionPaymentDto result = paymentService.recordPayment(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Boutique Test", result.getCompanyName());
        assertEquals("Août", result.getPeriodMonth());
        assertEquals(2026, result.getPeriodYear());
        assertEquals("ORANGE_MONEY", result.getPaymentMethod());
        assertTrue(company.isActive(), "L'entreprise doit être activée après paiement");

        verify(companyRepository).save(company);
        verify(paymentRepository).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("Devrait bloquer un paiement en double pour le même mois et la même année")
    void recordPayment_DuplicateMonth_ThrowsException() {
        SubscriptionPaymentDto dto = new SubscriptionPaymentDto();
        dto.setCompanyId(1L);
        dto.setAmount(new BigDecimal("30000"));
        dto.setPeriodMonth("Août");
        dto.setPeriodYear(2026);

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentRepository.existsByCompanyIdAndPeriodMonthIgnoreCaseAndPeriodYear(1L, "Août", 2026))
                .thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.recordPayment(dto);
        });

        assertTrue(exception.getMessage().contains("a déjà réglé son abonnement pour le mois de"));
        assertTrue(exception.getMessage().contains("Août 2026"));

        verify(paymentRepository, never()).save(any(SubscriptionPayment.class));
    }

    @Test
    @DisplayName("Devrait refuser un paiement avec un mois vide ou null")
    void recordPayment_InvalidMonth_ThrowsException() {
        SubscriptionPaymentDto dto = new SubscriptionPaymentDto();
        dto.setCompanyId(1L);
        dto.setAmount(new BigDecimal("30000"));
        dto.setPeriodMonth("   ");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.recordPayment(dto);
        });

        assertTrue(exception.getMessage().contains("Le mois de la période d'abonnement est obligatoire"));
    }

    @Test
    @DisplayName("Devrait refuser un paiement avec un montant négatif ou nul")
    void recordPayment_InvalidAmount_ThrowsException() {
        SubscriptionPaymentDto dto = new SubscriptionPaymentDto();
        dto.setCompanyId(1L);
        dto.setAmount(BigDecimal.ZERO);
        dto.setPeriodMonth("Août");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.recordPayment(dto);
        });

        assertTrue(exception.getMessage().contains("Le montant du paiement doit être supérieur à zéro"));
    }

    @Test
    @DisplayName("Devrait calculer automatiquement la date d'expiration (+1 mois) et réactiver l'entreprise")
    void recordPayment_CalculatesDatesAndUpdatesExpiration() {
        SubscriptionPaymentDto dto = new SubscriptionPaymentDto();
        dto.setCompanyId(1L);
        dto.setAmount(new BigDecimal("30000"));
        dto.setPeriodMonth("Septembre");
        dto.setPeriodYear(2026);
        dto.setPaymentMethod("WAVE");

        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(paymentRepository.existsByCompanyIdAndPeriodMonthIgnoreCaseAndPeriodYear(1L, "Septembre", 2026))
                .thenReturn(false);
        when(paymentRepository.save(any(SubscriptionPayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionPaymentDto result = paymentService.recordPayment(dto);

        assertNotNull(result);
        assertNotNull(result.getStartDate(), "La date de début doit être initialisée");
        assertNotNull(result.getEndDate(), "La date de fin doit être calculée");
        assertEquals(result.getStartDate().plusMonths(1), result.getEndDate(), "La période doit être de 1 mois");
        assertEquals(result.getEndDate(), company.getSubscriptionExpiresAt(), "L'entreprise doit avoir sa date d'expiration mise à jour");
        assertTrue(company.isActive(), "L'entreprise doit être active");
    }

    @Test
    @DisplayName("Devrait suspendre automatiquement les entreprises dont la date d'expiration + 3 jours de grâce est dépassée")
    void autoSuspendExpiredCompanies_SuspendsOnlyAfterGracePeriod() {
        Company expiredBeyondGrace = new Company();
        expiredBeyondGrace.setId(10L);
        expiredBeyondGrace.setName("Boutique Expirée");
        expiredBeyondGrace.setActive(true);
        expiredBeyondGrace.setSubscriptionExpiresAt(java.time.LocalDate.now().minusDays(4)); // Expiré il y a 4 jours (> 3 jours grâce)

        Company inGracePeriod = new Company();
        inGracePeriod.setId(20L);
        inGracePeriod.setName("Boutique En Grâce");
        inGracePeriod.setActive(true);
        inGracePeriod.setSubscriptionExpiresAt(java.time.LocalDate.now().minusDays(2)); // Expiré il y a 2 jours (<= 3 jours grâce)

        Company activeValid = new Company();
        activeValid.setId(30L);
        activeValid.setName("Boutique Valide");
        activeValid.setActive(true);
        activeValid.setSubscriptionExpiresAt(java.time.LocalDate.now().plusDays(15)); // Valide encore 15 jours

        when(companyRepository.findAll()).thenReturn(java.util.List.of(expiredBeyondGrace, inGracePeriod, activeValid));

        paymentService.autoSuspendExpiredCompanies();

        assertFalse(expiredBeyondGrace.isActive(), "L'entreprise expirée depuis plus de 3 jours doit être suspendue");
        assertTrue(inGracePeriod.isActive(), "L'entreprise en période de grâce doit rester active");
        assertTrue(activeValid.isActive(), "L'entreprise avec abonnement valide doit rester active");

        verify(companyRepository).save(expiredBeyondGrace);
        verify(companyRepository, never()).save(inGracePeriod);
        verify(companyRepository, never()).save(activeValid);
    }
}
