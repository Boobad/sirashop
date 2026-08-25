package com.sirashop.service;

import com.sirashop.dto.UserDto;
import com.sirashop.entity.Company;
import com.sirashop.entity.Role;
import com.sirashop.entity.Shop;
import com.sirashop.entity.User;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.ShopRepository;
import com.sirashop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private Company company;
    private Shop shop;
    private User user;

    @BeforeEach
    void setUp() {
        company = new Company();
        company.setId(1L);
        company.setName("Sira High-Tech");

        shop = new Shop();
        shop.setId(10L);
        shop.setName("Boutique Grand Marché");
        shop.setCompany(company);

        user = new User();
        user.setId(100L);
        user.setEmail("amadou.traore@sirashop.ml");
        user.setUsername("amadou.traore@sirashop.ml");
        user.setPassword("encoded_pwd");
        user.setFirstName("Amadou");
        user.setLastName("Traoré");
        user.setPhone("76000000");
        user.setRole(Role.SELLER);
        user.setCompany(company);
        user.setShop(shop);
        user.setActive(true);
    }

    @Test
    @DisplayName("Devrait créer un employé avec email unique, nom, prénom et téléphone")
    void createUser_Success() {
        UserDto dto = new UserDto();
        dto.setEmail("amadou.traore@sirashop.ml");
        dto.setPassword("secret123");
        dto.setFirstName("Amadou");
        dto.setLastName("Traoré");
        dto.setPhone("76000000");
        dto.setRole(Role.SELLER);
        dto.setCompanyId(1L);
        dto.setShopId(10L);

        when(userRepository.existsByEmailIgnoreCase("amadou.traore@sirashop.ml")).thenReturn(false);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(shopRepository.findById(10L)).thenReturn(Optional.of(shop));
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pwd");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto created = userService.createUser(dto);

        assertNotNull(created);
        assertEquals("amadou.traore@sirashop.ml", created.getEmail());
        assertEquals("Amadou", created.getFirstName());
        assertEquals("Traoré", created.getLastName());
        assertEquals("76000000", created.getPhone());
        assertEquals(Role.SELLER, created.getRole());
        assertTrue(created.isActive());

        verify(emailService).sendAccountCreatedEmailAsync(
                eq("amadou.traore@sirashop.ml"),
                eq("Amadou Traoré"),
                eq("secret123"),
                eq("SELLER"),
                eq("Sira High-Tech")
        );
    }

    @Test
    @DisplayName("Devrait rejeter la création si l'adresse email est déjà utilisée")
    void createUser_DuplicateEmail_ThrowsException() {
        UserDto dto = new UserDto();
        dto.setEmail("amadou.traore@sirashop.ml");
        dto.setPassword("secret123");

        when(userRepository.existsByEmailIgnoreCase("amadou.traore@sirashop.ml")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(dto));
        assertTrue(ex.getMessage().contains("est déjà utilisée"));
    }

    @Test
    @DisplayName("Devrait autoriser plusieurs employés ayant les mêmes Nom et Prénom tant que l'email est différent")
    void createUser_SameNameDifferentEmail_Success() {
        UserDto dto2 = new UserDto();
        dto2.setEmail("amadou2@sirashop.ml");
        dto2.setPassword("secret123");
        dto2.setFirstName("Amadou"); // Même prénom
        dto2.setLastName("Traoré");  // Même nom
        dto2.setPhone("76111111");

        User user2 = new User();
        user2.setId(101L);
        user2.setEmail("amadou2@sirashop.ml");
        user2.setFirstName("Amadou");
        user2.setLastName("Traoré");
        user2.setPhone("76111111");
        user2.setActive(true);

        when(userRepository.existsByEmailIgnoreCase("amadou2@sirashop.ml")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_pwd_2");
        when(userRepository.save(any(User.class))).thenReturn(user2);

        UserDto created = userService.createUser(dto2);

        assertNotNull(created);
        assertEquals("amadou2@sirashop.ml", created.getEmail());
        assertEquals("Amadou", created.getFirstName());
        assertEquals("Traoré", created.getLastName());
    }

    @Test
    @DisplayName("Devrait rejeter la création si le téléphone comporte moins de 8 chiffres")
    void createUser_InvalidPhone_ThrowsException() {
        UserDto dto = new UserDto();
        dto.setEmail("vendeur2@sirashop.ml");
        dto.setPassword("secret123");
        dto.setPhone("12345"); // Trop court

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(dto));
        assertTrue(ex.getMessage().contains("au moins 8 chiffres"));
    }

    @Test
    @DisplayName("Devrait modifier les coordonnées d'un employé avec succès")
    void updateUser_Success() {
        UserDto updateDto = new UserDto();
        updateDto.setFirstName("Amadou Junior");
        updateDto.setLastName("Traoré");
        updateDto.setPhone("66000000");

        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(100L, updateDto);

        assertEquals("Amadou Junior", result.getFirstName());
        assertEquals("66000000", result.getPhone());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Devrait bloquer ou débloquer un employé (soft toggle)")
    void toggleUserActive_Success() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1er clic : Actif -> Bloqué (false)
        UserDto result1 = userService.toggleUserActive(100L);
        assertFalse(result1.isActive(), "L'employé doit être désactivé/bloqué");

        // 2eme clic : Bloqué -> Débloqué (true)
        UserDto result2 = userService.toggleUserActive(100L);
        assertTrue(result2.isActive(), "L'employé doit être réactivé");
    }

    @Test
    @DisplayName("Devrait réinitialiser le mot de passe d'un employé")
    void resetUserPassword_Success() {
        when(userRepository.findById(100L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("nouveauPass123")).thenReturn("new_encoded_pwd");

        userService.resetUserPassword(100L, "nouveauPass123");

        assertEquals("new_encoded_pwd", user.getPassword());
        verify(userRepository).save(user);
    }
}
