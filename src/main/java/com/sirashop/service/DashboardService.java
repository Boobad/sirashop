package com.sirashop.service;

import com.sirashop.dto.ShopStatsDto;
import com.sirashop.dto.StatsDto;
import com.sirashop.entity.RepairStatus;
import com.sirashop.entity.RepairTicket;
import com.sirashop.entity.Sale;
import com.sirashop.entity.Shop;
import com.sirashop.repository.RepairTicketRepository;
import com.sirashop.repository.SaleRepository;
import com.sirashop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SaleRepository saleRepository;
    private final RepairTicketRepository repairTicketRepository;
    private final ShopRepository shopRepository;

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
}
