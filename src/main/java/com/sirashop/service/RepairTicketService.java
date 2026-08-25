package com.sirashop.service;

import com.sirashop.dto.RepairTicketDto;
import com.sirashop.entity.*;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.RepairTicketRepository;
import com.sirashop.repository.ShopRepository;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairTicketService {

    private final RepairTicketRepository repairTicketRepository;
    private final CompanyRepository companyRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    public RepairTicketDto createTicket(RepairTicketDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Le corps de la requête est vide.");
        }

        Shop shop = null;
        Company company = null;

        if (dto.getShopId() != null) {
            shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new RuntimeException("Boutique non trouvée (ID: " + dto.getShopId() + ")"));
            company = shop.getCompany();
        } else if (dto.getCompanyId() != null) {
            company = companyRepository.findById(dto.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Entreprise non trouvée (ID: " + dto.getCompanyId() + ")"));
        } else {
            throw new IllegalArgumentException("Veuillez fournir l'identifiant de la boutique (shopId) ou de l'entreprise (companyId).");
        }

        RepairTicket ticket = new RepairTicket();
        ticket.setCustomerName(dto.getCustomerName());
        ticket.setCustomerPhone(dto.getCustomerPhone());
        ticket.setDeviceModel(dto.getDeviceModel());
        ticket.setIssueDescription(dto.getIssueDescription());
        ticket.setEstimatedPrice(dto.getEstimatedPrice());
        ticket.setDepositAmount(dto.getDepositAmount());
        ticket.setStatus(RepairStatus.RECEIVED);
        ticket.setCompany(company);
        ticket.setShop(shop);

        if (dto.getTechnicianId() != null) {
            User tech = userRepository.findById(dto.getTechnicianId()).orElse(null);
            ticket.setTechnician(tech);
        }

        RepairTicket saved = repairTicketRepository.save(ticket);
        return mapToDto(saved);
    }

    public RepairTicketDto updateStatus(Long ticketId, RepairStatus newStatus, Long technicianId, BigDecimal depositAmount, Boolean payInFull) {
        RepairTicket ticket = repairTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé: " + ticketId));

        if (newStatus != null) {
            ticket.setStatus(newStatus);
        }

        if (technicianId != null) {
            User tech = userRepository.findById(technicianId).orElse(null);
            ticket.setTechnician(tech);
        }

        if (Boolean.TRUE.equals(payInFull)) {
            ticket.setDepositAmount(ticket.getEstimatedPrice());
        } else if (depositAmount != null) {
            ticket.setDepositAmount(depositAmount);
        }

        RepairTicket saved = repairTicketRepository.save(ticket);
        return mapToDto(saved);
    }

    public RepairTicketDto updatePayment(Long ticketId, BigDecimal newDepositAmount, BigDecimal additionalPayment, Boolean payInFull) {
        RepairTicket ticket = repairTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé: " + ticketId));

        if (Boolean.TRUE.equals(payInFull)) {
            ticket.setDepositAmount(ticket.getEstimatedPrice());
        } else if (newDepositAmount != null) {
            ticket.setDepositAmount(newDepositAmount);
        } else if (additionalPayment != null) {
            BigDecimal currentDeposit = ticket.getDepositAmount() != null ? ticket.getDepositAmount() : BigDecimal.ZERO;
            ticket.setDepositAmount(currentDeposit.add(additionalPayment));
        }

        RepairTicket saved = repairTicketRepository.save(ticket);
        return mapToDto(saved);
    }

    public RepairTicketDto updateTicket(Long ticketId, RepairTicketDto dto) {
        RepairTicket ticket = repairTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé: " + ticketId));

        if (dto.getCustomerName() != null) ticket.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerPhone() != null) ticket.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getDeviceModel() != null) ticket.setDeviceModel(dto.getDeviceModel());
        if (dto.getIssueDescription() != null) ticket.setIssueDescription(dto.getIssueDescription());
        if (dto.getEstimatedPrice() != null) ticket.setEstimatedPrice(dto.getEstimatedPrice());
        if (dto.getDepositAmount() != null) ticket.setDepositAmount(dto.getDepositAmount());
        if (dto.getStatus() != null) ticket.setStatus(dto.getStatus());

        if (dto.getTechnicianId() != null) {
            User tech = userRepository.findById(dto.getTechnicianId()).orElse(null);
            ticket.setTechnician(tech);
        }

        RepairTicket saved = repairTicketRepository.save(ticket);
        return mapToDto(saved);
    }

    public List<RepairTicketDto> getTicketsByShop(Long shopId) {
        return repairTicketRepository.findByShopId(shopId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<RepairTicketDto> getTicketsByCompany(Long companyId) {
        return repairTicketRepository.findByCompanyId(companyId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private RepairTicketDto mapToDto(RepairTicket ticket) {
        RepairTicketDto dto = new RepairTicketDto();
        dto.setId(ticket.getId());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setCustomerPhone(ticket.getCustomerPhone());
        dto.setDeviceModel(ticket.getDeviceModel());
        dto.setIssueDescription(ticket.getIssueDescription());
        dto.setEstimatedPrice(ticket.getEstimatedPrice());
        dto.setDepositAmount(ticket.getDepositAmount());
        dto.setStatus(ticket.getStatus());
        dto.setCompanyId(ticket.getCompany().getId());
        dto.setShopId(ticket.getShop().getId());
        dto.setShopName(ticket.getShop().getName());

        if (ticket.getTechnician() != null) {
            dto.setTechnicianId(ticket.getTechnician().getId());
            dto.setTechnicianUsername(ticket.getTechnician().getUsername());
        }

        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        return dto;
    }
}
