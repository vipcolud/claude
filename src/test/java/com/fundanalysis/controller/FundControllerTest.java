package com.fundanalysis.controller;

import com.fundanalysis.dto.FundComparisonRequest;
import com.fundanalysis.dto.FundDTO;
import com.fundanalysis.dto.FundDetailDTO;
import com.fundanalysis.dto.FundMetricsDTO;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.service.FundAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FundController.class)
class FundControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FundAnalysisService analysisService;

    @Test
    void testGetAllFunds() throws Exception {
        FundDTO fundDTO = new FundDTO("TEST001", "测试基金", FundType.MIXED, "测试公司", "描述");
        when(analysisService.getAllFunds()).thenReturn(List.of(fundDTO));

        mockMvc.perform(get("/api/funds"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TEST001"))
                .andExpect(jsonPath("$[0].name").value("测试基金"));
    }

    @Test
    void testGetFundDetail() throws Exception {
        FundDetailDTO detailDTO = FundDetailDTO.builder()
                .code("TEST001")
                .name("测试基金")
                .type(FundType.MIXED)
                .manager("测试公司")
                .build();

        when(analysisService.getFundDetail("TEST001")).thenReturn(detailDTO);

        mockMvc.perform(get("/api/funds/TEST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("TEST001"))
                .andExpect(jsonPath("$.name").value("测试基金"));
    }

    @Test
    void testGetFundMetrics() throws Exception {
        FundMetricsDTO metricsDTO = FundMetricsDTO.builder()
                .fundCode("TEST001")
                .fundName("测试基金")
                .cumulativeReturn(BigDecimal.valueOf(50))
                .annualizedReturn(BigDecimal.valueOf(20))
                .build();

        when(analysisService.getFundMetrics("TEST001")).thenReturn(metricsDTO);

        mockMvc.perform(get("/api/funds/TEST001/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fundCode").value("TEST001"))
                .andExpect(jsonPath("$.cumulativeReturn").value(50));
    }

    @Test
    void testCompareFunds() throws Exception {
        String requestJson = "{\"fundCodes\":[\"TEST001\",\"TEST002\"]}";

        when(analysisService.compareFunds(any())).thenReturn(null);

        mockMvc.perform(post("/api/funds/compare")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void testFilterFunds() throws Exception {
        FundDTO fundDTO = new FundDTO("TEST001", "测试基金", FundType.STOCK, "测试公司", "描述");
        when(analysisService.filterFunds(any(), any())).thenReturn(List.of(fundDTO));

        mockMvc.perform(get("/api/funds/filter")
                .param("type", "STOCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("TEST001"));
    }
}
