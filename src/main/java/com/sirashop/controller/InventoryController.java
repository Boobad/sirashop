package com.sirashop.controller;

import com.sirashop.dto.InventoryDto;
import com.sirashop.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/set-stock")
    public ResponseEntity<InventoryDto> setStock(
            @RequestParam Long shopId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam(required = false) Integer alertThreshold) {
        return ResponseEntity.ok(inventoryService.setStock(shopId, productId, quantity, alertThreshold));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<InventoryDto>> getInventoryByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(inventoryService.getInventoryByShop(shopId));
    }

    @GetMapping("/product/{productId}/network")
    public ResponseEntity<List<InventoryDto>> getNetworkStockByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getNetworkStockByProduct(productId));
    }
}
