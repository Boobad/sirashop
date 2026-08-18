package com.sirashop.service;

import com.sirashop.dto.ShopDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Shop;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final CompanyRepository companyRepository;

    public ShopDto createShop(ShopDto dto) {
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));

        Shop shop = new Shop();
        shop.setName(dto.getName());
        shop.setAddress(dto.getAddress());
        shop.setCompany(company);

        Shop saved = shopRepository.save(shop);
        return mapToDto(saved);
    }

    public List<ShopDto> getShopsByCompany(Long companyId) {
        return shopRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ShopDto mapToDto(Shop shop) {
        ShopDto dto = new ShopDto();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setAddress(shop.getAddress());
        dto.setCompanyId(shop.getCompany().getId());
        dto.setCreatedAt(shop.getCreatedAt());
        return dto;
    }
}
