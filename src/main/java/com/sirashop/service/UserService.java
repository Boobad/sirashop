package com.sirashop.service;

import com.sirashop.dto.ChangePasswordDto;
import com.sirashop.dto.UserDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Shop;
import com.sirashop.entity.User;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.ShopRepository;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserDto createUser(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setActive(true);

        String companyName = null;
        if (dto.getCompanyId() != null) {
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
            user.setCompany(company);
            companyName = company.getName();
        }

        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));
            user.setShop(shop);
        }

        User saved = userRepository.save(user);

        // Envoi de l'email avec les identifiants à l'utilisateur
        String roleDescription = (dto.getRole() != null) ? dto.getRole().name() : "Utilisateur";
        emailService.sendAccountCreatedEmailAsync(
                dto.getUsername(),
                dto.getUsername(),
                dto.getPassword(),
                roleDescription,
                companyName
        );

        return mapToDto(saved);
    }

    public void changePassword(ChangePasswordDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
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

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Utilisateur non trouvé: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public UserDto toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
        user.setActive(!user.isActive());
        User saved = userRepository.save(user);
        return mapToDto(saved);
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
