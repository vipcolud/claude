package com.fundanalysis.service;

import com.fundanalysis.dto.FundMetricsDTO;
import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.repository.FundNetValueRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundMetricsServiceTest {

    @Mock
    private FundNetValueRepository netValueRepository;

    @InjectMocks
    private FundMetricsService metricsService;

    private Fund testFund;
    private List<FundNetValue> testNetValues;

    @BeforeEach
    void setUp() {
        testFund = new Fund("TEST001", "测试基金", FundType.MIXED, "测试基金公司", "测试描述");
        testFund.setId(1L);
        
        testNetValues = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        
        for (int i = 0; i < 100; i++) {
            BigDecimal netValue = BigDecimal.valueOf(1.0 + i * 0.01);
            FundNetValue fnv = new FundNetValue(
                    testFund,
                    startDate.plusDays(i),
                    netValue,
                    netValue
            );
            testNetValues.add(fnv);
        }
    }

    @Test
    void testCalculateMetrics_WithValidData() {
        when(netValueRepository.findByFundOrderByValueDateAsc(testFund))
                .thenReturn(testNetValues);

        FundMetricsDTO metrics = metricsService.calculateMetrics(testFund);

        assertNotNull(metrics);
        assertEquals("TEST001", metrics.getFundCode());
        assertEquals("测试基金", metrics.getFundName());
        assertNotNull(metrics.getCumulativeReturn());
        assertNotNull(metrics.getAnnualizedReturn());
        assertNotNull(metrics.getVolatility());
        assertNotNull(metrics.getSharpeRatio());
        assertNotNull(metrics.getMaxDrawdown());
        assertEquals(100, metrics.getDataPoints());
    }

    @Test
    void testCalculateMetrics_WithEmptyData() {
        when(netValueRepository.findByFundOrderByValueDateAsc(testFund))
                .thenReturn(new ArrayList<>());

        FundMetricsDTO metrics = metricsService.calculateMetrics(testFund);

        assertNotNull(metrics);
        assertEquals("TEST001", metrics.getFundCode());
        assertEquals(BigDecimal.ZERO, metrics.getCumulativeReturn());
        assertEquals(BigDecimal.ZERO, metrics.getAnnualizedReturn());
        assertEquals(0, metrics.getDataPoints());
    }

    @Test
    void testCalculateCumulativeReturn() {
        when(netValueRepository.findByFundOrderByValueDateAsc(testFund))
                .thenReturn(testNetValues);

        FundMetricsDTO metrics = metricsService.calculateMetrics(testFund);

        assertTrue(metrics.getCumulativeReturn().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testCalculateVolatility() {
        when(netValueRepository.findByFundOrderByValueDateAsc(testFund))
                .thenReturn(testNetValues);

        FundMetricsDTO metrics = metricsService.calculateMetrics(testFund);

        assertTrue(metrics.getVolatility().compareTo(BigDecimal.ZERO) >= 0);
    }
}
