package com.sirashop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // ex: MDS

    private String phone; // Téléphone de contact

    private String ownerName; // Nom du propriétaire / gérant

    // Modularité des fonctionnalités
    private boolean hasSalesEnabled = true; // Module Vente & Stock activé

    private boolean hasRepairsEnabled = false; // Module SAV & Réparations activé

    private boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
