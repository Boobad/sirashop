package com.sirashop.service;

import com.sirashop.dto.UserDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Shop;
import com.sirashop.entity.User;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.ShopRepository;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;

    public UserDto createUser(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword()); // Attention: on devra utiliser BCryptEncoder ici plus tard !
        user.setRole(dto.getRole());
        user.setActive(true);

        // Si l'utilisateur appartient à une entreprise
        if (dto.getCompanyId() != null) {
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
            user.setCompany(company);
        }

        // Si l'utilisateur appartient à une boutique précise (vendeur, technicien...)
        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));
            user.setShop(shop);
        }

        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    public List<UserDto> getUsersByCompany(Long companyId) {
        return userRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public List<UserDto> getUsersByShop(Long shopId) {
        return userRepository.findByShopId(shopId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        
        if (user.getCompany() != null) {
            dto.setCompanyId(user.getCompany().getId());
        }
        if (user.getShop() != null) {
            dto.setShopId(user.getShop().getId());
        }
        return dto;
    }
}
