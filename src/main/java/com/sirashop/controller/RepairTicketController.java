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
        RepairStatus status = RepairStatus.valueOf(payload.get("status").toString());
        Long technicianId = payload.get("technicianId") != null ? Long.valueOf(payload.get("technicianId").toString()) : null;

        return ResponseEntity.ok(repairTicketService.updateStatus(id, status, technicianId));
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
