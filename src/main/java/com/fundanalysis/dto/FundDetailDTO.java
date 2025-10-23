package com.fundanalysis.dto;

import com.fundanalysis.entity.FundType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundDetailDTO {
    private String code;
    private String name;
    private FundType type;
    private String manager;
    private String description;
    private BigDecimal latestNetValue;
    private LocalDate latestDate;
    private Integer totalDataPoints;
}
