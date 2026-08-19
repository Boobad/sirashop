package com.sirashop.controller;

import com.sirashop.dto.SaleDto;
import com.sirashop.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleDto> processSale(@RequestBody SaleDto saleDto) {
        return ResponseEntity.ok(saleService.processSale(saleDto));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<SaleDto>> getSalesByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(saleService.getSalesByShop(shopId));
    }
}
