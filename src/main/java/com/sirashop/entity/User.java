package com.sirashop.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String username;

    @Column(nullable = false)
    private String password;

    private String firstName;

    private String lastName;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Le Super Admin n'a pas d'entreprise. Les autres oui.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    // Le propriétaire de l'entreprise n'est pas lié à une boutique spécifique (il voit tout).
    // Les employés (vendeur, technicien) sont liés à une boutique.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    private boolean isActive = true;

    // Indique si l'utilisateur a des identifiants et le droit de se connecter à l'application
    private boolean hasAppAccess = true;

    @Column(nullable = false)
    private boolean mustChangePassword = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
