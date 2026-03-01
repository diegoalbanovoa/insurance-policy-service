package com.insurance.domain.repository;

import com.insurance.domain.model.Policy;
import com.insurance.domain.model.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByClientId(Long clientId);
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByClientIdAndPolicyType(Long clientId, PolicyType policyType);
    Optional<Policy> findFirstByClientIdAndPolicyType(Long clientId, PolicyType policyType);
    long countByClientIdAndPolicyType(Long clientId, PolicyType policyType);
}
