package com.sirashop.controller;

import com.sirashop.dto.RepairTicketDto;
import com.sirashop.entity.RepairStatus;
import com.sirashop.service.RepairTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/repairs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RepairTicketController {

    private final RepairTicketService repairTicketService;

    @PostMapping
    public ResponseEntity<RepairTicketDto> createTicket(@RequestBody RepairTicketDto dto) {
        return ResponseEntity.ok(repairTicketService.createTicket(dto));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<RepairTicketDto> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        RepairStatus status = payload.get("status") != null ? RepairStatus.valueOf(payload.get("status").toString()) : null;
        Long technicianId = payload.get("technicianId") != null ? Long.valueOf(payload.get("technicianId").toString()) : null;
        java.math.BigDecimal depositAmount = payload.get("depositAmount") != null ? new java.math.BigDecimal(payload.get("depositAmount").toString()) : null;
        Boolean payInFull = payload.get("payInFull") != null ? Boolean.valueOf(payload.get("payInFull").toString()) : null;

        return ResponseEntity.ok(repairTicketService.updateStatus(id, status, technicianId, depositAmount, payInFull));
    }

    @PutMapping("/{id}/payment")
    public ResponseEntity<RepairTicketDto> updatePayment(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload
    ) {
        java.math.BigDecimal depositAmount = payload.get("depositAmount") != null ? new java.math.BigDecimal(payload.get("depositAmount").toString()) : null;
        java.math.BigDecimal additionalPayment = payload.get("additionalPayment") != null ? new java.math.BigDecimal(payload.get("additionalPayment").toString()) : null;
        Boolean payInFull = payload.get("payInFull") != null ? Boolean.valueOf(payload.get("payInFull").toString()) : null;

        return ResponseEntity.ok(repairTicketService.updatePayment(id, depositAmount, additionalPayment, payInFull));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepairTicketDto> updateTicket(
            @PathVariable Long id,
            @RequestBody RepairTicketDto dto
    ) {
        return ResponseEntity.ok(repairTicketService.updateTicket(id, dto));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<RepairTicketDto>> getTicketsByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(repairTicketService.getTicketsByShop(shopId));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<RepairTicketDto>> getTicketsByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(repairTicketService.getTicketsByCompany(companyId));
    }
}
