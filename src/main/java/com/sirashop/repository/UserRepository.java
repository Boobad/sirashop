package com.sirashop.repository;

import com.sirashop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    List<User> findByCompanyId(Long companyId);
    List<User> findByShopId(Long shopId);
}
