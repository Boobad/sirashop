package com.sirashop.service;

import com.sirashop.dto.CompanyDto;
import com.sirashop.dto.CompanyRegistrationDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Role;
import com.sirashop.entity.User;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public CompanyDto createCompany(CompanyDto dto) {
        Company company = new Company();
        company.setName(dto.getName());
        company.setActive(true);
        
        Company saved = companyRepository.save(company);
        return mapToDto(saved);
    }

    public CompanyDto createCompanyWithOwner(CompanyRegistrationDto dto) {
        Company company = new Company();
        company.setName(dto.getCompanyName());
        company.setActive(true);
        Company savedCompany = companyRepository.save(company);


        User owner = new User();
        owner.setUsername(dto.getOwnerUsername());
        owner.setPassword(passwordEncoder.encode(dto.getOwnerPassword()));
        owner.setRole(Role.COMPANY_OWNER);
        owner.setCompany(savedCompany);
        owner.setActive(true);
        userRepository.save(owner);


        emailService.sendAccountCreatedEmailAsync(
                dto.getOwnerUsername(),
                dto.getOwnerUsername(),
                dto.getOwnerPassword(),
                "Propriétaire d'entreprise",
                savedCompany.getName()
        );

        return mapToDto(savedCompany);
    }

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CompanyDto toggleCompanyActive(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + companyId));

        company.setActive(!company.isActive());
        Company saved = companyRepository.save(company);
        return mapToDto(saved);
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
