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
        // L'email est l'identifiant unique obligatoire
        String rawEmail = dto.getEmail() != null && !dto.getEmail().trim().isEmpty() 
                ? dto.getEmail().trim() 
                : (dto.getUsername() != null ? dto.getUsername().trim() : null);

        if (rawEmail == null || rawEmail.isEmpty()) {
            throw new RuntimeException("L'adresse email est obligatoire pour créer un compte.");
        }

        String cleanEmail = rawEmail.toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(cleanEmail)) {
            throw new RuntimeException("L'adresse email '" + cleanEmail + "' est déjà utilisée par un autre compte.");
        }

        if (dto.getPassword() == null || dto.getPassword().trim().length() < 6) {
            throw new RuntimeException("Le mot de passe doit comporter au moins 6 caractères.");
        }
        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty() && dto.getPhone().trim().length() < 8) {
            throw new RuntimeException("Le numéro de téléphone doit comporter au moins 8 chiffres.");
        }

        String cleanUsername = (dto.getUsername() != null && !dto.getUsername().trim().isEmpty())
                ? dto.getUsername().trim()
                : cleanEmail;

        User user = new User();
        user.setEmail(cleanEmail);
        user.setUsername(cleanUsername);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        // Le nom et le prénom ne sont PAS uniques (plusieurs personnes peuvent avoir le même nom)
        user.setFirstName(dto.getFirstName() != null ? dto.getFirstName().trim() : null);
        user.setLastName(dto.getLastName() != null ? dto.getLastName().trim() : null);
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
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
        String recipientDisplayName = (dto.getFirstName() != null && dto.getLastName() != null) 
                ? (dto.getFirstName() + " " + dto.getLastName()) 
                : cleanUsername;

        emailService.sendAccountCreatedEmailAsync(
                cleanEmail,
                recipientDisplayName,
                dto.getPassword(),
                roleDescription,
                companyName
        );

        return mapToDto(saved);
    }

    public UserDto updateUser(Long userId, UserDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'identifiant: " + userId));

        // Mise à jour de l'email avec vérification d'unicité
        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim().toLowerCase();
            userRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.getId().equals(userId)) {
                    throw new RuntimeException("L'adresse email '" + newEmail + "' est déjà utilisée par un autre compte.");
                }
            });
            user.setEmail(newEmail);
        }

        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            user.setUsername(dto.getUsername().trim());
        }

        // Le nom et prénom peuvent être modifiés librement sans contrainte d'unicité
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName().trim());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName().trim());
        }
        if (dto.getPhone() != null) {
            if (!dto.getPhone().trim().isEmpty() && dto.getPhone().trim().length() < 8) {
                throw new RuntimeException("Le numéro de téléphone doit comporter au moins 8 chiffres.");
            }
            user.setPhone(dto.getPhone().trim());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }

        if (dto.getShopId() != null) {
            Shop shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Boutique non trouvée: " + dto.getShopId()));
            user.setShop(shop);
        }

        if (dto.getCompanyId() != null) {
            Company company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée: " + dto.getCompanyId()));
            user.setCompany(company);
        }

        User saved = userRepository.save(user);
        return mapToDto(saved);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));
        return mapToDto(user);
    }

    public void resetUserPassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new RuntimeException("Le nouveau mot de passe doit comporter au moins 6 caractères.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);
    }

    public void changePassword(ChangePasswordDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Ancien mot de passe incorrect");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().trim().length() < 6) {
            throw new RuntimeException("Le nouveau mot de passe doit comporter au moins 6 caractères.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword().trim()));
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
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername() != null ? user.getUsername() : user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        
        if (user.getCompany() != null) {
            dto.setCompanyId(user.getCompany().getId());
        }
        if (user.getShop() != null) {
            dto.setShopId(user.getShop().getId());
            dto.setShopName(user.getShop().getName());
        }
        return dto;
    }
}

