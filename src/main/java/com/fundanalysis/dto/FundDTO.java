package com.fundanalysis.dto;

import com.fundanalysis.entity.FundType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundDTO {
    private String code;
    private String name;
    private FundType type;
    private String manager;
    private String description;
}
