package com.sirashop.service;

import com.sirashop.dto.SubscriptionPaymentDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.SubscriptionPayment;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.SubscriptionPaymentRepository;
import lombok.RequiredArgsConstructor;
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

        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setCompany(company);
        payment.setAmount(dto.getAmount());
        payment.setPeriodMonth(cleanMonth);
        payment.setPeriodYear(cleanYear);
        payment.setNotes(dto.getNotes());

        // Quand le paiement de l'abonnement est enregistré, débloquer automatiquement l'entreprise !
        company.setActive(true);
        companyRepository.save(company);

        SubscriptionPayment saved = paymentRepository.save(payment);
        return mapToDto(saved);
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
        dto.setNotes(p.getNotes());
        dto.setPaymentDate(p.getPaymentDate());
        return dto;
    }
}
