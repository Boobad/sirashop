package com.sirashop.service;

import com.sirashop.dto.RepairTicketDto;
import com.sirashop.entity.*;
import com.sirashop.repository.CompanyRepository;
import com.sirashop.repository.RepairTicketRepository;
import com.sirashop.repository.ShopRepository;
import com.sirashop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Entreprise non trouvée"));
        Shop shop = shopRepository.findById(dto.getShopId())
                .orElseThrow(() -> new RuntimeException("Boutique non trouvée"));

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

    public RepairTicketDto updateStatus(Long ticketId, RepairStatus newStatus, Long technicianId) {
        RepairTicket ticket = repairTicketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket non trouvé: " + ticketId));

        ticket.setStatus(newStatus);
        if (technicianId != null) {
            User tech = userRepository.findById(technicianId).orElse(null);
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
