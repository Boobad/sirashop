package com.sirashop.repository;

import com.sirashop.entity.RepairTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairTicketRepository extends JpaRepository<RepairTicket, Long> {
    List<RepairTicket> findByShopId(Long shopId);
    List<RepairTicket> findByCompanyId(Long companyId);
    List<RepairTicket> findByTechnicianId(Long technicianId);
}
