package com.fundanalysis.repository;

import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FundNetValueRepository extends JpaRepository<FundNetValue, Long> {
    
    List<FundNetValue> findByFundOrderByValueDateAsc(Fund fund);
    
    List<FundNetValue> findByFundAndValueDateBetweenOrderByValueDateAsc(
        Fund fund, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT fnv FROM FundNetValue fnv WHERE fnv.fund = :fund " +
           "AND fnv.valueDate >= :startDate ORDER BY fnv.valueDate ASC")
    List<FundNetValue> findByFundSinceDate(
        @Param("fund") Fund fund, @Param("startDate") LocalDate startDate);
    
    void deleteByFund(Fund fund);
}
