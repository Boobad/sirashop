package com.sirashop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
    name = "subscription_payments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_company_subscription_period", columnNames = {"company_id", "period_month", "period_year"})
    }
)
public class SubscriptionPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private BigDecimal amount; // ex: 30000 FCFA

    @Column(name = "period_month", nullable = false)
    private String periodMonth; // ex: Août, Septembre...

    @Column(name = "period_year", nullable = false)
    private Integer periodYear; // ex: 2026

    private String notes;

    @CreationTimestamp
    private LocalDateTime paymentDate;
}
