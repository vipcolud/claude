package com.fundanalysis.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundComparisonRequest {
    
    @NotEmpty(message = "基金代码列表不能为空")
    @Size(min = 2, max = 10, message = "对比基金数量应在2-10个之间")
    private List<String> fundCodes;
}
