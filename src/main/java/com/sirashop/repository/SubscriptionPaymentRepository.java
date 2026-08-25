package com.sirashop.repository;

import com.sirashop.entity.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    List<SubscriptionPayment> findByCompanyId(Long companyId);

    boolean existsByCompanyIdAndPeriodMonthIgnoreCaseAndPeriodYear(Long companyId, String periodMonth, Integer periodYear);

    List<SubscriptionPayment> findByCompanyIdAndPeriodMonthIgnoreCaseAndPeriodYear(Long companyId, String periodMonth, Integer periodYear);
}
