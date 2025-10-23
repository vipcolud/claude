package com.fundanalysis.repository;

import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {
    
    Optional<Fund> findByCode(String code);
    
    List<Fund> findByType(FundType type);
    
    List<Fund> findByManager(String manager);
    
    boolean existsByCode(String code);
}
