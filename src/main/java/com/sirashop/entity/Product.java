package com.sirashop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // ex: iPhone 13 128Go, Chargeur Type-C 20W

    private String description;

    private String barcode; // Code-barres ou numéro de série IMEI

    @Column(nullable = false)
    private BigDecimal purchasePrice; // Prix d'achat (pour calculer les marges)

    @Column(nullable = false)
    private BigDecimal sellingPrice; // Prix de vente au client

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // Produit lié au catalogue global de l'entreprise

    @CreationTimestamp
    private LocalDateTime createdAt;
}
