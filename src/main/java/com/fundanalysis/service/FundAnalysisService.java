package com.fundanalysis.service;

import com.fundanalysis.dto.*;
import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.repository.FundNetValueRepository;
import com.fundanalysis.repository.FundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundAnalysisService {

    private final FundRepository fundRepository;
    private final FundNetValueRepository netValueRepository;
    private final FundMetricsService metricsService;

    public List<FundDTO> getAllFunds() {
        return fundRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public FundDetailDTO getFundDetail(String code) {
        Fund fund = fundRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("基金未找到: " + code));

        List<FundNetValue> netValues = netValueRepository.findByFundOrderByValueDateAsc(fund);

        return FundDetailDTO.builder()
                .code(fund.getCode())
                .name(fund.getName())
                .type(fund.getType())
                .manager(fund.getManager())
                .description(fund.getDescription())
                .latestNetValue(netValues.isEmpty() ? null : 
                    netValues.get(netValues.size() - 1).getNetValue())
                .latestDate(netValues.isEmpty() ? null : 
                    netValues.get(netValues.size() - 1).getValueDate())
                .totalDataPoints(netValues.size())
                .build();
    }

    public FundMetricsDTO getFundMetrics(String code) {
        Fund fund = fundRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("基金未找到: " + code));
        
        return metricsService.calculateMetrics(fund);
    }

    public FundComparisonResponse compareFunds(List<String> fundCodes) {
        List<FundMetricsDTO> metricsLis = fundCodes.stream()
                .map(code -> {
                    try {
                        return getFundMetrics(code);
                    } catch (RuntimeException e) {
                        log.warn("Failed to get metrics for fund: {}", code, e);
                        return null;
                    }
                })
                .filter(metrics -> metrics != null)
                .collect(Collectors.toList());

        return new FundComparisonResponse(metricsLis);
    }

    public List<FundDTO> filterFunds(FundType type, BigDecimal minReturn) {
        List<Fund> funds;
        
        if (type != null) {
            funds = fundRepository.findByType(type);
        } else {
            funds = fundRepository.findAll();
        }

        if (minReturn != null) {
            funds = funds.stream()
                    .filter(fund -> {
                        FundMetricsDTO metrics = metricsService.calculateMetrics(fund);
                        return metrics.getAnnualizedReturn().compareTo(minReturn) >= 0;
                    })
                    .collect(Collectors.toList());
        }

        return funds.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FundMetricsDTO> rankFundsByReturn() {
        return fundRepository.findAll().stream()
                .map(metricsService::calculateMetrics)
                .sorted(Comparator.comparing(FundMetricsDTO::getAnnualizedReturn).reversed())
                .collect(Collectors.toList());
    }

    public List<FundMetricsDTO> rankFundsBySharpeRatio() {
        return fundRepository.findAll().stream()
                .map(metricsService::calculateMetrics)
                .sorted(Comparator.comparing(FundMetricsDTO::getSharpeRatio).reversed())
                .collect(Collectors.toList());
    }

    private FundDTO convertToDTO(Fund fund) {
        return new FundDTO(
                fund.getCode(),
                fund.getName(),
                fund.getType(),
                fund.getManager(),
                fund.getDescription()
        );
    }
}
