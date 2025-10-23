package com.fundanalysis.service;

import com.fundanalysis.dto.FundComparisonResponse;
import com.fundanalysis.dto.FundDTO;
import com.fundanalysis.dto.FundDetailDTO;
import com.fundanalysis.dto.FundMetricsDTO;
import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.repository.FundNetValueRepository;
import com.fundanalysis.repository.FundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundAnalysisServiceTest {

    @Mock
    private FundRepository fundRepository;

    @Mock
    private FundNetValueRepository netValueRepository;

    @Mock
    private FundMetricsService metricsService;

    @InjectMocks
    private FundAnalysisService analysisService;

    private Fund testFund;
    private List<FundNetValue> testNetValues;

    @BeforeEach
    void setUp() {
        testFund = new Fund("TEST001", "测试基金", FundType.MIXED, "测试基金公司", "测试描述");
        testFund.setId(1L);

        testNetValues = new ArrayList<>();
        FundNetValue fnv = new FundNetValue(
                testFund,
                LocalDate.now(),
                BigDecimal.valueOf(1.5),
                BigDecimal.valueOf(1.5)
        );
        testNetValues.add(fnv);
    }

    @Test
    void testGetAllFunds() {
        List<Fund> funds = List.of(testFund);
        when(fundRepository.findAll()).thenReturn(funds);

        List<FundDTO> result = analysisService.getAllFunds();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TEST001", result.get(0).getCode());
    }

    @Test
    void testGetFundDetail_Success() {
        when(fundRepository.findByCode("TEST001")).thenReturn(Optional.of(testFund));
        when(netValueRepository.findByFundOrderByValueDateAsc(testFund)).thenReturn(testNetValues);

        FundDetailDTO detail = analysisService.getFundDetail("TEST001");

        assertNotNull(detail);
        assertEquals("TEST001", detail.getCode());
        assertEquals("测试基金", detail.getName());
        assertEquals(FundType.MIXED, detail.getType());
    }

    @Test
    void testGetFundDetail_NotFound() {
        when(fundRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            analysisService.getFundDetail("INVALID");
        });
    }

    @Test
    void testGetFundMetrics() {
        FundMetricsDTO expectedMetrics = FundMetricsDTO.builder()
                .fundCode("TEST001")
                .fundName("测试基金")
                .cumulativeReturn(BigDecimal.valueOf(50))
                .annualizedReturn(BigDecimal.valueOf(20))
                .build();

        when(fundRepository.findByCode("TEST001")).thenReturn(Optional.of(testFund));
        when(metricsService.calculateMetrics(testFund)).thenReturn(expectedMetrics);

        FundMetricsDTO result = analysisService.getFundMetrics("TEST001");

        assertNotNull(result);
        assertEquals("TEST001", result.getFundCode());
    }

    @Test
    void testCompareFunds() {
        Fund fund1 = new Fund("TEST001", "基金1", FundType.STOCK, "公司1", "描述1");
        Fund fund2 = new Fund("TEST002", "基金2", FundType.BOND, "公司2", "描述2");

        FundMetricsDTO metrics1 = FundMetricsDTO.builder()
                .fundCode("TEST001")
                .fundName("基金1")
                .build();
        FundMetricsDTO metrics2 = FundMetricsDTO.builder()
                .fundCode("TEST002")
                .fundName("基金2")
                .build();

        when(fundRepository.findByCode("TEST001")).thenReturn(Optional.of(fund1));
        when(fundRepository.findByCode("TEST002")).thenReturn(Optional.of(fund2));
        when(metricsService.calculateMetrics(fund1)).thenReturn(metrics1);
        when(metricsService.calculateMetrics(fund2)).thenReturn(metrics2);

        FundComparisonResponse response = analysisService.compareFunds(List.of("TEST001", "TEST002"));

        assertNotNull(response);
        assertEquals(2, response.getFunds().size());
    }

    @Test
    void testFilterFunds_ByType() {
        when(fundRepository.findByType(FundType.MIXED)).thenReturn(List.of(testFund));

        List<FundDTO> result = analysisService.filterFunds(FundType.MIXED, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(FundType.MIXED, result.get(0).getType());
    }
}
