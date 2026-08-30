package com.sirashop.service;

import com.sirashop.dto.SubscriptionPaymentDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.SubscriptionPayment;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentService {

    private final SubscriptionPaymentRepository paymentRepository;
    private final CompanyRepository companyRepository;

    public SubscriptionPaymentDto recordPayment(SubscriptionPaymentDto dto) {
        if (dto.getCompanyId() == null) {
            throw new RuntimeException("L'identifiant de l'entreprise est obligatoire pour enregistrer un paiement.");
        }
        if (dto.getPeriodMonth() == null || dto.getPeriodMonth().trim().isEmpty()) {
            throw new RuntimeException("Le mois de la période d'abonnement est obligatoire.");
        }
        if (dto.getAmount() == null || dto.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Le montant du paiement doit être supérieur à zéro.");
        }

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));

        String cleanMonth = dto.getPeriodMonth().trim();
        int cleanYear = (dto.getPeriodYear() != null && dto.getPeriodYear() > 0) 
                ? dto.getPeriodYear() 
                : LocalDate.now().getYear();

        // Vérifier si un paiement a déjà été effectué pour cette entreprise, ce mois et cette année
        boolean alreadyPaid = paymentRepository.existsByCompanyIdAndPeriodMonthIgnoreCaseAndPeriodYear(
                company.getId(),
                cleanMonth,
                cleanYear
        );

        if (alreadyPaid) {
            throw new RuntimeException("L'entreprise '" + company.getName() + "' a déjà réglé son abonnement pour le mois de " + cleanMonth + " " + cleanYear + ".");
        }

        // Calcul automatique des dates d'abonnement (1 mois)
        LocalDate startDate;
        if (company.getSubscriptionExpiresAt() != null && company.getSubscriptionExpiresAt().isAfter(LocalDate.now())) {
            startDate = company.getSubscriptionExpiresAt(); // S'ajoute à la suite
        } else {
            startDate = LocalDate.now();
        }
        LocalDate endDate = startDate.plusMonths(1);

        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setCompany(company);
        payment.setAmount(dto.getAmount());
        payment.setPeriodMonth(cleanMonth);
        payment.setPeriodYear(cleanYear);
        payment.setPaymentMethod(dto.getPaymentMethod() != null && !dto.getPaymentMethod().trim().isEmpty()
                ? dto.getPaymentMethod().trim().toUpperCase()
                : "CASH");
        payment.setStartDate(startDate);
        payment.setEndDate(endDate);
        payment.setNotes(dto.getNotes());

        // Quand le paiement est enregistré : réactiver l'entreprise et mettre à jour la date d'expiration
        company.setActive(true);
        company.setSubscriptionExpiresAt(endDate);
        companyRepository.save(company);

        SubscriptionPayment saved = paymentRepository.save(payment);
        return mapToDto(saved);
    }

    /**
     * Tâche planifiée automatique : s'exécute chaque nuit à 01:00.
     * Vérifie toutes les entreprises actives : si la date d'expiration + 3 jours de grâce est dépassée,
     * l'entreprise est automatiquement suspendue (isActive = false).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoSuspendExpiredCompanies() {
        LocalDate now = LocalDate.now();
        List<Company> activeCompanies = companyRepository.findAll().stream()
                .filter(Company::isActive)
                .collect(Collectors.toList());

        for (Company company : activeCompanies) {
            if (company.getSubscriptionExpiresAt() != null) {
                LocalDate gracePeriodEnd = company.getSubscriptionExpiresAt().plusDays(3);
                if (now.isAfter(gracePeriodEnd)) {
                    company.setActive(false);
                    companyRepository.save(company);
                    System.out.println("⚠️ [AUTO-SUSPENSION] Entreprise '" + company.getName() 
                            + "' suspendue automatiquement. Expiration: " + company.getSubscriptionExpiresAt() 
                            + ", Fin de grâce: " + gracePeriodEnd);
                }
            }
        }
    }

    public List<SubscriptionPaymentDto> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SubscriptionPaymentDto> getPaymentsByCompany(Long companyId) {
        return paymentRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SubscriptionPaymentDto mapToDto(SubscriptionPayment p) {
        SubscriptionPaymentDto dto = new SubscriptionPaymentDto();
        dto.setId(p.getId());
        dto.setCompanyId(p.getCompany().getId());
        dto.setCompanyName(p.getCompany().getName());
        dto.setAmount(p.getAmount());
        dto.setPeriodMonth(p.getPeriodMonth());
        dto.setPeriodYear(p.getPeriodYear());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setNotes(p.getNotes());
        dto.setPaymentDate(p.getPaymentDate());
        return dto;
    }
}
