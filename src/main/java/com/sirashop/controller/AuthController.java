package com.sirashop.controller;

import com.sirashop.dto.LoginRequest;
import com.sirashop.dto.LoginResponse;
import com.sirashop.service.AuthService;
import com.sirashop.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint de diagnostic pour tester l'envoi d'email directement.
     * Exemple d'appel : GET http://localhost:8085/api/auth/test-email?to=monami@gmail.com
     */
    @GetMapping("/test-email")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam String to) {
        try {
            emailService.sendAccountCreatedEmail(to, to, "MonMotDePasse123", "Utilisateur Test", "SiraShop");
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Email envoyé avec succès à " + to,
                    "expediteur", "bakarydiallo312@gmail.com"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", "Échec de l'envoi de l'email : " + e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().getMessage() : "Inconnue"
            ));
        }
    }
}

