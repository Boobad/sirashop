package com.sirashop.repository;

import com.sirashop.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByShopId(Long shopId);
    Optional<Inventory> findByShopIdAndProductId(Long shopId, Long productId);
    List<Inventory> findByProductId(Long productId);
}
