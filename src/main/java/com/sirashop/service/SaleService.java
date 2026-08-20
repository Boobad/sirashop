package com.sirashop.service;

import com.sirashop.dto.SaleDto;
import com.sirashop.dto.SaleItemDto;
import com.sirashop.entity.*;
import com.sirashop.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Transactional
    public SaleDto processSale(SaleDto dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));
        User seller = userRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));

        Sale sale = new Sale();
        sale.setCompany(company);
        sale.setShop(shop);
        sale.setSeller(seller);
        sale.setPaymentMethod(dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "CASH");

        BigDecimal totalSaleAmount = BigDecimal.ZERO;

        for (SaleItemDto itemDto : dto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produit non trouvé: " + itemDto.getProductId()));

            Inventory inventory = inventoryRepository.findByShopIdAndProductId(shop.getId(), product.getId())
                    .orElseThrow(() -> new RuntimeException("Pas de stock configuré pour " + product.getName() + " dans cette boutique"));

            if (inventory.getQuantity() < itemDto.getQuantity()) {
                throw new RuntimeException("Stock insuffisant pour " + product.getName() + " (Disponible: " + inventory.getQuantity() + ")");
            }

            inventory.setQuantity(inventory.getQuantity() - itemDto.getQuantity());
            inventoryRepository.save(inventory);

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            
            // Si le prix unitaire a été négocié en caisse, l'utiliser sinon prendre le prix du catalogue
            BigDecimal unitPrice = (itemDto.getUnitPrice() != null && itemDto.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) 
                    ? itemDto.getUnitPrice() : product.getSellingPrice();
            item.setUnitPrice(unitPrice);

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));
            item.setTotalPrice(lineTotal);

            totalSaleAmount = totalSaleAmount.add(lineTotal);
            sale.getItems().add(item);
        }

        sale.setTotalAmount(totalSaleAmount);
        Sale saved = saleRepository.save(sale);

        return mapToDto(saved);
    }

    public List<SaleDto> getSalesByShop(Long shopId) {
        return saleRepository.findByShopId(shopId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SaleDto> getSalesByCompany(Long companyId) {
        return saleRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SaleDto mapToDto(Sale sale) {
        SaleDto dto = new SaleDto();
        dto.setId(sale.getId());
        dto.setCompanyId(sale.getCompany().getId());
        dto.setShopId(sale.getShop().getId());
        dto.setShopName(sale.getShop().getName());
        dto.setSellerId(sale.getSeller().getId());
        dto.setSellerUsername(sale.getSeller().getUsername());
        dto.setTotalAmount(sale.getTotalAmount());
        dto.setPaymentMethod(sale.getPaymentMethod());
        dto.setCreatedAt(sale.getCreatedAt());

        dto.setItems(sale.getItems().stream().map(item -> {
            SaleItemDto itemDto = new SaleItemDto();
            itemDto.setId(item.getId());
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setUnitPrice(item.getUnitPrice());
            itemDto.setTotalPrice(item.getTotalPrice());
            return itemDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
