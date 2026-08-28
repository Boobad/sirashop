package com.sirashop.service;

import com.sirashop.dto.*;
import com.sirashop.entity.*;
import com.sirashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final RepairTicketRepository repairTicketRepository;
    private final ShopRepository shopRepository;

    // =========================================
    // Stats de base (existant)
    // =========================================
    public StatsDto getCompanyStats(Long companyId) {
        List<Sale> companySales = saleRepository.findByCompanyId(companyId);
        List<RepairTicket> companyRepairs = repairTicketRepository.findByCompanyId(companyId);
        List<Shop> companyShops = shopRepository.findByCompanyId(companyId);

        BigDecimal totalSalesRevenue = companySales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRepairRevenue = companyRepairs.stream()
                .filter(t -> t.getStatus() == RepairStatus.DELIVERED || t.getStatus() == RepairStatus.REPAIRED)
                .map(t -> t.getEstimatedPrice() != null ? t.getEstimatedPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StatsDto stats = new StatsDto();
        stats.setSalesRevenue(totalSalesRevenue);
        stats.setRepairRevenue(totalRepairRevenue);
        stats.setTotalRevenue(totalSalesRevenue.add(totalRepairRevenue));
        stats.setTotalSalesCount((long) companySales.size());
        stats.setTotalRepairsCount((long) companyRepairs.size());

        List<ShopStatsDto> shopStatsList = new ArrayList<>();
        for (Shop shop : companyShops) {
            ShopStatsDto shopDto = new ShopStatsDto();
            shopDto.setShopId(shop.getId());
            shopDto.setShopName(shop.getName());

            List<Sale> shopSales = companySales.stream()
                    .filter(s -> s.getShop().getId().equals(shop.getId()))
                    .toList();

            List<RepairTicket> shopRepairs = companyRepairs.stream()
                    .filter(r -> r.getShop().getId().equals(shop.getId()))
                    .toList();

            BigDecimal shopSalesRev = shopSales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shopRepairRev = shopRepairs.stream()
                    .filter(t -> t.getStatus() == RepairStatus.DELIVERED || t.getStatus() == RepairStatus.REPAIRED)
                    .map(t -> t.getEstimatedPrice() != null ? t.getEstimatedPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            shopDto.setSalesRevenue(shopSalesRev);
            shopDto.setSalesCount((long) shopSales.size());
            shopDto.setRepairRevenue(shopRepairRev);
            shopDto.setRepairsCount((long) shopRepairs.size());
            shopDto.setTotalRevenue(shopSalesRev.add(shopRepairRev));

            shopStatsList.add(shopDto);
        }

        stats.setShopStats(shopStatsList);
        return stats;
    }

    // =========================================
    // Dashboard avancé avec graphiques
    // =========================================
    public AdvancedStatsDto getAdvancedStats(Long companyId) {
        List<Sale> companySales = saleRepository.findByCompanyId(companyId);
        List<RepairTicket> companyRepairs = repairTicketRepository.findByCompanyId(companyId);
        List<Shop> companyShops = shopRepository.findByCompanyId(companyId);

        // Stats de base
        BigDecimal totalSalesRevenue = companySales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRepairRevenue = companyRepairs.stream()
                .filter(t -> t.getStatus() == RepairStatus.DELIVERED || t.getStatus() == RepairStatus.REPAIRED)
                .map(t -> t.getEstimatedPrice() != null ? t.getEstimatedPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        AdvancedStatsDto stats = new AdvancedStatsDto();
        stats.setSalesRevenue(totalSalesRevenue);
        stats.setRepairRevenue(totalRepairRevenue);
        stats.setTotalRevenue(totalSalesRevenue.add(totalRepairRevenue));
        stats.setTotalSalesCount((long) companySales.size());
        stats.setTotalRepairsCount((long) companyRepairs.size());
        // Stats par boutique
        stats.setShopStats(buildShopStats(companySales, companyRepairs, companyShops));

        // Ventes par jour (30 derniers jours)
        stats.setDailyRevenue(buildDailyRevenue(companySales, companyRepairs));

        // Top 5 produits les plus vendus
        stats.setTopProducts(buildTopProducts(companySales));

        // Performance des vendeurs
        stats.setSellerPerformance(buildSellerPerformance(companySales));

        // Répartition des méthodes de paiement
        stats.setPaymentMethods(buildPaymentMethodStats(companySales));

        // Comparaison mois en cours vs mois précédent
        buildMonthComparison(stats, companyId);

        return stats;
    }

    // =========================================
    // Ventes par jour (30 derniers jours)
    // =========================================
    private List<DailyRevenueDto> buildDailyRevenue(List<Sale> sales, List<RepairTicket> repairs) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(29); // 30 jours inclus aujourd'hui

        List<DailyRevenueDto> dailyList = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = thirtyDaysAgo.plusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            // Ventes du jour
            List<Sale> daySales = sales.stream()
                    .filter(s -> s.getCreatedAt() != null
                            && !s.getCreatedAt().isBefore(startOfDay)
                            && !s.getCreatedAt().isAfter(endOfDay))
                    .toList();

            BigDecimal daySalesRevenue = daySales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Réparations terminées du jour
            List<RepairTicket> dayRepairs = repairs.stream()
                    .filter(r -> r.getUpdatedAt() != null
                            && !r.getUpdatedAt().isBefore(startOfDay)
                            && !r.getUpdatedAt().isAfter(endOfDay)
                            && (r.getStatus() == RepairStatus.DELIVERED || r.getStatus() == RepairStatus.REPAIRED))
                    .toList();

            BigDecimal dayRepairRevenue = dayRepairs.stream()
                    .map(r -> r.getEstimatedPrice() != null ? r.getEstimatedPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DailyRevenueDto dayDto = new DailyRevenueDto();
            dayDto.setDate(date);
            dayDto.setSalesRevenue(daySalesRevenue);
            dayDto.setRepairRevenue(dayRepairRevenue);
            dayDto.setTotalRevenue(daySalesRevenue.add(dayRepairRevenue));
            dayDto.setSalesCount((long) daySales.size());
            dayDto.setRepairsCount((long) dayRepairs.size());

            dailyList.add(dayDto);
        }

        return dailyList;
    }

    // =========================================
    // Top 5 produits les plus vendus
    // =========================================
    private List<TopProductDto> buildTopProducts(List<Sale> sales) {
        // Collecter tous les SaleItems de toutes les ventes
        Map<Long, TopProductDto> productMap = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getItems() == null) continue;
            for (SaleItem item : sale.getItems()) {
                Long productId = item.getProduct().getId();
                TopProductDto existing = productMap.get(productId);

                if (existing == null) {
                    existing = new TopProductDto();
                    existing.setProductId(productId);
                    existing.setProductName(item.getProduct().getName());
                    existing.setTotalQuantitySold(0L);
                    existing.setTotalRevenue(BigDecimal.ZERO);
                    productMap.put(productId, existing);
                }

                existing.setTotalQuantitySold(existing.getTotalQuantitySold() + item.getQuantity());
                existing.setTotalRevenue(existing.getTotalRevenue().add(item.getTotalPrice()));
            }
        }

        // Trier par quantité vendue (décroissant) et prendre le top 5
        return productMap.values().stream()
                .sorted(Comparator.comparingLong(TopProductDto::getTotalQuantitySold).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    // =========================================
    // Performance des vendeurs
    // =========================================
    private List<SellerPerformanceDto> buildSellerPerformance(List<Sale> sales) {
        Map<Long, SellerPerformanceDto> sellerMap = new HashMap<>();

        for (Sale sale : sales) {
            if (sale.getSeller() == null) continue;
            Long sellerId = sale.getSeller().getId();
            SellerPerformanceDto existing = sellerMap.get(sellerId);

            if (existing == null) {
                existing = new SellerPerformanceDto();
                existing.setSellerId(sellerId);
                existing.setSellerName(sale.getSeller().getUsername());
                existing.setTotalSalesCount(0L);
                existing.setTotalRevenue(BigDecimal.ZERO);
                sellerMap.put(sellerId, existing);
            }

            existing.setTotalSalesCount(existing.getTotalSalesCount() + 1);
            existing.setTotalRevenue(existing.getTotalRevenue().add(sale.getTotalAmount()));
        }

        // Trier par chiffre d'affaires (décroissant)
        return sellerMap.values().stream()
                .sorted(Comparator.comparing(SellerPerformanceDto::getTotalRevenue).reversed())
                .collect(Collectors.toList());
    }

    // =========================================
    // Répartition des méthodes de paiement
    // =========================================
    private List<PaymentMethodStatsDto> buildPaymentMethodStats(List<Sale> sales) {
        long totalSales = sales.size();
        Map<String, List<Sale>> grouped = sales.stream()
                .collect(Collectors.groupingBy(s ->
                        s.getPaymentMethod() != null ? s.getPaymentMethod() : "NON_SPECIFIE"));

        List<PaymentMethodStatsDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Sale>> entry : grouped.entrySet()) {
            PaymentMethodStatsDto dto = new PaymentMethodStatsDto();
            dto.setPaymentMethod(entry.getKey());
            dto.setCount((long) entry.getValue().size());
            dto.setTotalAmount(entry.getValue().stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            dto.setPercentage(totalSales > 0
                    ? (double) entry.getValue().size() / totalSales * 100
                    : 0.0);
            result.add(dto);
        }

        // Trier par montant total (décroissant)
        result.sort(Comparator.comparing(PaymentMethodStatsDto::getTotalAmount).reversed());
        return result;
    }

    // =========================================
    // Comparaison mois en cours vs mois précédent
    // =========================================
    private void buildMonthComparison(AdvancedStatsDto stats, Long companyId) {
        LocalDate now = LocalDate.now();

        // Mois en cours
        LocalDateTime currentMonthStart = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime currentMonthEnd = now.atTime(LocalTime.MAX);
        List<Sale> currentMonthSales = saleRepository.findByCompanyIdAndCreatedAtBetween(
                companyId, currentMonthStart, currentMonthEnd);
        BigDecimal currentMonthRevenue = currentMonthSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Mois précédent
        LocalDate previousMonth = now.minusMonths(1);
        LocalDateTime prevMonthStart = previousMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevMonthEnd = previousMonth.withDayOfMonth(previousMonth.lengthOfMonth()).atTime(LocalTime.MAX);
        List<Sale> prevMonthSales = saleRepository.findByCompanyIdAndCreatedAtBetween(
                companyId, prevMonthStart, prevMonthEnd);
        BigDecimal previousMonthRevenue = prevMonthSales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setCurrentMonthRevenue(currentMonthRevenue);
        stats.setPreviousMonthRevenue(previousMonthRevenue);

        // Calcul du pourcentage de croissance
        if (previousMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal growth = currentMonthRevenue.subtract(previousMonthRevenue)
                    .divide(previousMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            stats.setRevenueGrowthPercentage(growth.doubleValue());
        } else if (currentMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            stats.setRevenueGrowthPercentage(100.0); // De 0 à quelque chose = +100%
        } else {
            stats.setRevenueGrowthPercentage(0.0);
        }
    }

    // =========================================
    // Stats par boutique (réutilisé)
    // =========================================
    private List<ShopStatsDto> buildShopStats(List<Sale> sales, List<RepairTicket> repairs, List<Shop> shops) {
        List<ShopStatsDto> shopStatsList = new ArrayList<>();
        for (Shop shop : shops) {
            ShopStatsDto shopDto = new ShopStatsDto();
            shopDto.setShopId(shop.getId());
            shopDto.setShopName(shop.getName());

            List<Sale> shopSales = sales.stream()
                    .filter(s -> s.getShop().getId().equals(shop.getId()))
                    .toList();

            List<RepairTicket> shopRepairs = repairs.stream()
                    .filter(r -> r.getShop().getId().equals(shop.getId()))
                    .toList();

            BigDecimal shopSalesRev = shopSales.stream()
                    .map(Sale::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shopRepairRev = shopRepairs.stream()
                    .filter(t -> t.getStatus() == RepairStatus.DELIVERED || t.getStatus() == RepairStatus.REPAIRED)
                    .map(t -> t.getEstimatedPrice() != null ? t.getEstimatedPrice() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            shopDto.setSalesRevenue(shopSalesRev);
            shopDto.setSalesCount((long) shopSales.size());
            shopDto.setRepairRevenue(shopRepairRev);
            shopDto.setRepairsCount((long) shopRepairs.size());
            shopDto.setTotalRevenue(shopSalesRev.add(shopRepairRev));

            shopStatsList.add(shopDto);
        }
        return shopStatsList;
    }
}
