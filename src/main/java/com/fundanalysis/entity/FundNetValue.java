package com.fundanalysis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fund_net_values", indexes = {
    @Index(name = "idx_fund_date", columnList = "fund_id,value_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundNetValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_id", nullable = false)
    private Fund fund;

    @Column(name = "value_date", nullable = false)
    private LocalDate valueDate;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal netValue;

    @Column(precision = 10, scale = 4)
    private BigDecimal accumulatedValue;

    public FundNetValue(Fund fund, LocalDate valueDate, BigDecimal netValue, BigDecimal accumulatedValue) {
        this.fund = fund;
        this.valueDate = valueDate;
        this.netValue = netValue;
        this.accumulatedValue = accumulatedValue;
    }
}
