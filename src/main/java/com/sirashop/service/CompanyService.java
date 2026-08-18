package com.sirashop.service;

import com.sirashop.dto.CompanyDto;
import com.sirashop.entity.Company;
import com.sirashop.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyDto createCompany(CompanyDto dto) {
        Company company = new Company();
        company.setName(dto.getName());
        company.setActive(true);
        
        Company saved = companyRepository.save(company);
        return mapToDto(saved);
    }

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CompanyDto mapToDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setActive(company.isActive());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}
