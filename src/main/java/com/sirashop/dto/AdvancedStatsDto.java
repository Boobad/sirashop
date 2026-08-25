package com.sirashop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AdvancedStatsDto {
    // Stats globales existantes
    private BigDecimal totalRevenue;
    private BigDecimal salesRevenue;
    private BigDecimal repairRevenue;
    private Long totalSalesCount;
    private Long totalRepairsCount;

    // Stats par boutique
    private List<ShopStatsDto> shopStats;

    // Nouvelles stats avancées
    private List<DailyRevenueDto> dailyRevenue;         // Ventes par jour (30 derniers jours)
    private List<TopProductDto> topProducts;              // Top 5 produits les plus vendus
    private List<SellerPerformanceDto> sellerPerformance; // Classement des vendeurs
    private List<PaymentMethodStatsDto> paymentMethods;   // Répartition CASH / Orange Money / Moov / Card

    // Comparaison avec le mois précédent
    private BigDecimal currentMonthRevenue;
    private BigDecimal previousMonthRevenue;
    private Double revenueGrowthPercentage; // ex: +15.3% ou -5.2%
}
