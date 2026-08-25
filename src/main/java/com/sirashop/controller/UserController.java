package com.sirashop.controller;

import com.sirashop.dto.ChangePasswordDto;
import com.sirashop.dto.UserDto;
import com.sirashop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody ChangePasswordDto dto) {
        userService.changePassword(dto);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
    }

    @PostMapping("/{id}/reset-password")
    public ResponseEntity<Map<String, String>> resetUserPassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("newPassword") != null ? payload.get("newPassword") : payload.get("password");
        userService.resetUserPassword(id, newPassword);
        return ResponseEntity.ok(Map.of("message", "Mot de passe de l'utilisateur réinitialisé avec succès"));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<UserDto>> getUsersByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(userService.getUsersByCompany(companyId));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<UserDto>> getUsersByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(userService.getUsersByShop(shopId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserDto> patchToggleUserActive(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserActive(id));
    }

    @PutMapping("/{id}/toggle-active")
    public ResponseEntity<UserDto> toggleUserActive(@PathVariable Long id) {
        return ResponseEntity.ok(userService.toggleUserActive(id));
    }
}

