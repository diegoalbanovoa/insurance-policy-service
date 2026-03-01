package com.insurance.domain.repository;

import com.insurance.domain.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {
    List<Beneficiary> findByPolicyId(Long policyId);
    long countByPolicyId(Long policyId);
}
