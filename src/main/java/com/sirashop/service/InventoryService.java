package com.sirashop.service;

import com.sirashop.dto.InventoryDto;
import com.sirashop.entity.Inventory;
import com.sirashop.entity.Product;
import com.sirashop.entity.Shop;
import com.sirashop.repository.InventoryRepository;
import com.sirashop.repository.ProductRepository;
import com.sirashop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public InventoryDto setStock(Long shopId, Long productId, Integer quantity, Integer alertThreshold) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produit non trouvé"));

        Inventory inventory = inventoryRepository.findByShopIdAndProductId(shopId, productId)
                .orElseGet(() -> {
                    Inventory inv = new Inventory();
                    inv.setShop(shop);
                    inv.setProduct(product);
                    return inv;
                });

        inventory.setQuantity(quantity);
        if (alertThreshold != null) {
            inventory.setAlertThreshold(alertThreshold);
        }

        Inventory saved = inventoryRepository.save(inventory);
        return mapToDto(saved);
    }

    public List<InventoryDto> getInventoryByShop(Long shopId) {
        return inventoryRepository.findByShopId(shopId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private InventoryDto mapToDto(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProduct().getId());
        dto.setProductName(inventory.getProduct().getName());
        dto.setShopId(inventory.getShop().getId());
        dto.setShopName(inventory.getShop().getName());
        dto.setQuantity(inventory.getQuantity());
        dto.setAlertThreshold(inventory.getAlertThreshold());
        return dto;
    }
}
