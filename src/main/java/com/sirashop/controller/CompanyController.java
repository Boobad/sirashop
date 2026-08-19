package com.sirashop.controller;

import com.sirashop.dto.CompanyDto;
import com.sirashop.dto.CompanyRegistrationDto;
import com.sirashop.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@RequestBody CompanyDto companyDto) {
        return ResponseEntity.ok(companyService.createCompany(companyDto));
    }

    @PostMapping("/with-owner")
    public ResponseEntity<CompanyDto> createCompanyWithOwner(@RequestBody CompanyRegistrationDto dto) {
        return ResponseEntity.ok(companyService.createCompanyWithOwner(dto));
    }

    @GetMapping
    public ResponseEntity<List<CompanyDto>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<CompanyDto> toggleCompanyActive(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.toggleCompanyActive(id));
    }
}
