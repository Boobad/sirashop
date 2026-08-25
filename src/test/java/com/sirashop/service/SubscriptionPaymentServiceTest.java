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
        savedPayment.setNotes(dto.getNotes());
        savedPayment.setPaymentDate(LocalDateTime.now());

        when(paymentRepository.save(any(SubscriptionPayment.class))).thenReturn(savedPayment);

        SubscriptionPaymentDto result = paymentService.recordPayment(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Boutique Test", result.getCompanyName());
        assertEquals("Août", result.getPeriodMonth());
        assertEquals(2026, result.getPeriodYear());
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
}
