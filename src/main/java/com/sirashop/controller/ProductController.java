package com.sirashop.controller;

import com.sirashop.dto.ProductDto;
import com.sirashop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto dto) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<ProductDto>> getProductsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(productService.getProductsByCompany(companyId));
    }
}
