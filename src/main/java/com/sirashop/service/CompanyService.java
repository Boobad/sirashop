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
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new RuntimeException("Le nom de l'entreprise est obligatoire.");
        }

        Company company = new Company();
        company.setName(dto.getName().trim());
        company.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        company.setOwnerName(dto.getOwnerName() != null ? dto.getOwnerName().trim() : null);
        company.setHasSalesEnabled(dto.isHasSalesEnabled());
        company.setHasRepairsEnabled(dto.isHasRepairsEnabled());
        company.setActive(true);
        
        Company saved = companyRepository.save(company);
        return mapToDto(saved);
    }

    public CompanyDto createCompanyWithOwner(CompanyRegistrationDto dto) {
        if (dto.getCompanyName() == null || dto.getCompanyName().trim().isEmpty()) {
            throw new RuntimeException("Le nom de l'entreprise est obligatoire.");
        }

        String ownerEmail = dto.getOwnerEmail() != null && !dto.getOwnerEmail().trim().isEmpty()
                ? dto.getOwnerEmail().trim().toLowerCase()
                : (dto.getOwnerUsername() != null ? dto.getOwnerUsername().trim().toLowerCase() : null);

        if (ownerEmail == null) {
            throw new RuntimeException("L'adresse email du propriétaire est obligatoire.");
        }

        if (userRepository.existsByEmailIgnoreCase(ownerEmail)) {
            throw new RuntimeException("L'adresse email '" + ownerEmail + "' est déjà utilisée par un autre compte.");
        }

        Company company = new Company();
        company.setName(dto.getCompanyName().trim());
        company.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        company.setOwnerName(dto.getOwnerName() != null ? dto.getOwnerName().trim() : null);
        company.setHasSalesEnabled(dto.isHasSalesEnabled());
        company.setHasRepairsEnabled(dto.isHasRepairsEnabled());
        company.setActive(true);

        Company savedCompany = companyRepository.save(company);

        String rawOwnerPassword = (dto.getOwnerPassword() != null && !dto.getOwnerPassword().trim().isEmpty())
                ? dto.getOwnerPassword().trim()
                : UserService.DEFAULT_PASSWORD;

        User owner = new User();
        owner.setEmail(ownerEmail);
        owner.setUsername(ownerEmail);
        owner.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        owner.setPassword(passwordEncoder.encode(rawOwnerPassword));
        owner.setMustChangePassword(true);
        owner.setRole(Role.COMPANY_OWNER);
        owner.setCompany(savedCompany);
        owner.setActive(true);

        if (dto.getOwnerName() != null && !dto.getOwnerName().trim().isEmpty()) {
            String[] parts = dto.getOwnerName().trim().split("\\s+", 2);
            owner.setFirstName(parts[0]);
            if (parts.length > 1) {
                owner.setLastName(parts[1]);
            }
        }

        userRepository.save(owner);

        emailService.sendAccountCreatedEmailAsync(
                ownerEmail,
                dto.getOwnerName() != null ? dto.getOwnerName() : ownerEmail,
                rawOwnerPassword,
                "Propriétaire d'entreprise",
                savedCompany.getName()
        );

        return mapToDto(savedCompany);
    }

    public CompanyDto updateCompany(Long companyId, CompanyDto dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + companyId));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            company.setName(dto.getName().trim());
        }
        if (dto.getPhone() != null) {
            company.setPhone(dto.getPhone().trim());
        }
        if (dto.getOwnerName() != null) {
            company.setOwnerName(dto.getOwnerName().trim());
        }
        
        company.setHasSalesEnabled(dto.isHasSalesEnabled());
        company.setHasRepairsEnabled(dto.isHasRepairsEnabled());

        Company saved = companyRepository.save(company);
        return mapToDto(saved);
    }

    public CompanyDto getCompanyById(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + companyId));
        return mapToDto(company);
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
        dto.setPhone(company.getPhone());
        dto.setOwnerName(company.getOwnerName());
        dto.setHasSalesEnabled(company.isHasSalesEnabled());
        dto.setHasRepairsEnabled(company.isHasRepairsEnabled());
        dto.setActive(company.isActive());
        dto.setCreatedAt(company.getCreatedAt());
        return dto;
    }
}

