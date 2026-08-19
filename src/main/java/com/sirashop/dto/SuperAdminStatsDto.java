package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SuperAdminStatsDto {
    private long totalCompanies;
    private long activeCompanies;
    private long blockedCompanies;
    private long totalShops;
    private long totalUsers;
    
    // Statistiques d'Abonnement SaaS (Revenu de l'éditeur SiraShop)
    private BigDecimal monthlyTariff = new BigDecimal("30000"); // Tarif mensuel par entreprise
    private BigDecimal expectedMonthlyRevenue; // CA Mensuel Attendu (ex: 50 * 30 000 = 1 500 000 FCFA)
    private BigDecimal totalSubscriptionRevenue; // CA Total Encaissé en Abonnements
    private BigDecimal currentMonthSubscriptionRevenue; // CA Encaissé ce mois-ci
}
