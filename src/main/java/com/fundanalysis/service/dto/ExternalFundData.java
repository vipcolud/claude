package com.fundanalysis.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalFundData {

    private String code;
    private String name;
    private String type;
    private String manager;
    @JsonProperty("fundCompany")
    private String fundCompany;
    @JsonProperty("netWorthData")
    private List<List<Object>> netWorthData = Collections.emptyList();
    @JsonProperty("netValueData")
    private List<List<Object>> netValueData = Collections.emptyList();
    @JsonProperty("fundScale")
    private String fundScale;
    @JsonProperty("fundRiskLevel")
    private String fundRiskLevel;
}
