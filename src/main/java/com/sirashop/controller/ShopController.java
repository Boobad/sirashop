package com.sirashop.controller;

import com.sirashop.dto.ShopDto;
import com.sirashop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ShopController {

    private final ShopService shopService;

    // Accessible par le Propriétaire de l'entreprise
    @PostMapping
    public ResponseEntity<ShopDto> createShop(@RequestBody ShopDto shopDto) {
        return ResponseEntity.ok(shopService.createShop(shopDto));
    }

    // Accessible par le Propriétaire de l'entreprise
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ShopDto>> getShopsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(shopService.getShopsByCompany(companyId));
    }
}
