package com.sirashop.repository;

import com.sirashop.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByShopId(Long shopId);
    List<Sale> findByCompanyId(Long companyId);
}
