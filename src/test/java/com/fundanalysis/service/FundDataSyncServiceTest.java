package com.fundanalysis.service;

import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.repository.FundNetValueRepository;
import com.fundanalysis.repository.FundRepository;
import com.fundanalysis.service.dto.ExternalFundData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundDataSyncServiceTest {

    @Mock
    private FundDataApiClient apiClient;

    @Mock
    private FundRepository fundRepository;

    @Mock
    private FundNetValueRepository netValueRepository;

    @InjectMocks
    private FundDataSyncService syncService;

    private ExternalFundData externalFundData;

    @BeforeEach
    void setUp() {
        externalFundData = new ExternalFundData();
        externalFundData.setCode("000001");
        externalFundData.setName("测试基金");
        externalFundData.setType("混合型");
        externalFundData.setManager("测试基金公司");
        externalFundData.setFundCompany("测试基金管理有限公司");
        externalFundData.setFundScale("100亿元");
        externalFundData.setFundRiskLevel("中风险");
        externalFundData.setNetWorthData(List.of(
                List.of("2023-01-01", "1.2340", "1.2340"),
                List.of("2023-01-02", "1.2400", "1.2400")
        ));
    }

    @Test
    void shouldSynchronizeFundWhenApiReturnsData() {
        when(apiClient.fetchFundDetail("000001")).thenReturn(Optional.of(externalFundData));
        when(fundRepository.findByCode("000001")).thenReturn(Optional.empty());
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> {
            Fund fund = invocation.getArgument(0);
            fund.setId(1L);
            return fund;
        });

        boolean result = syncService.synchronizeFund("000001");

        assertTrue(result);
        ArgumentCaptor<List<FundNetValue>> captor = ArgumentCaptor.forClass(List.class);
        verify(netValueRepository).saveAll(captor.capture());
        List<FundNetValue> savedNetValues = captor.getValue();
        assertThat(savedNetValues).hasSize(2);
        assertThat(savedNetValues.get(0).getNetValue()).isEqualByComparingTo(new BigDecimal("1.2340"));
        verify(netValueRepository).deleteByFund(any(Fund.class));
        verify(fundRepository, times(1)).save(any(Fund.class));
    }

    @Test
    void shouldUpdateExistingFund() {
        Fund existing = new Fund("000001", "旧基金", FundType.BOND, "旧管理人", "旧描述");
        existing.setId(5L);
        when(apiClient.fetchFundDetail("000001")).thenReturn(Optional.of(externalFundData));
        when(fundRepository.findByCode("000001")).thenReturn(Optional.of(existing));
        when(fundRepository.save(any(Fund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = syncService.synchronizeFund("000001");

        assertTrue(result);
        verify(netValueRepository).deleteByFund(eq(existing));
        verify(netValueRepository).saveAll(any());
        verify(fundRepository, times(1)).save(any(Fund.class));
    }

    @Test
    void shouldReturnFalseWhenApiFails() {
        when(apiClient.fetchFundDetail("000001")).thenReturn(Optional.empty());

        boolean result = syncService.synchronizeFund("000001");

        assertFalse(result);
        verify(netValueRepository, never()).saveAll(any());
    }
}
