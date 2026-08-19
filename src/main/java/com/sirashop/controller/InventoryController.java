package com.sirashop.controller;

import com.sirashop.dto.InventoryDto;
import com.sirashop.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/set")
    public ResponseEntity<InventoryDto> setStock(@RequestBody Map<String, Object> payload) {
        Long shopId = Long.valueOf(payload.get("shopId").toString());
        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = Integer.valueOf(payload.get("quantity").toString());
        Integer alertThreshold = payload.get("alertThreshold") != null ? Integer.valueOf(payload.get("alertThreshold").toString()) : 5;

        return ResponseEntity.ok(inventoryService.setStock(shopId, productId, quantity, alertThreshold));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<InventoryDto>> getInventoryByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(inventoryService.getInventoryByShop(shopId));
    }
}
