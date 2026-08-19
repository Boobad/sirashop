package com.sirashop.controller;

import com.sirashop.dto.SubscriptionPaymentDto;
import com.sirashop.dto.SuperAdminStatsDto;
import com.sirashop.dto.UserDto;
import com.sirashop.service.SubscriptionPaymentService;
import com.sirashop.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final SubscriptionPaymentService paymentService;

    @GetMapping("/stats")
    public ResponseEntity<SuperAdminStatsDto> getStats() {
        return ResponseEntity.ok(superAdminService.getStats());
    }

    @PostMapping("/admins")
    public ResponseEntity<UserDto> createSuperAdmin(@RequestBody UserDto dto) {
        return ResponseEntity.ok(superAdminService.createSuperAdmin(dto));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<UserDto>> getSuperAdmins() {
        return ResponseEntity.ok(superAdminService.getSuperAdmins());
    }

    @PostMapping("/subscriptions/pay")
    public ResponseEntity<SubscriptionPaymentDto> recordPayment(@RequestBody SubscriptionPaymentDto dto) {
        return ResponseEntity.ok(paymentService.recordPayment(dto));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<SubscriptionPaymentDto>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
}
