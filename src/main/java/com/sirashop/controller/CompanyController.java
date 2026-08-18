package com.sirashop.controller;

import com.sirashop.dto.CompanyDto;
import com.sirashop.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Permet à Angular de communiquer avec l'API
public class CompanyController {

    private final CompanyService companyService;

    // Accessible uniquement par le Super Admin (on sécurisera plus tard)
    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@RequestBody CompanyDto companyDto) {
        return ResponseEntity.ok(companyService.createCompany(companyDto));
    }

    // Accessible uniquement par le Super Admin
    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }
}
