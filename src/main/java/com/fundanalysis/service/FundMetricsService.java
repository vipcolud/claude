package com.fundanalysis.service;

import com.fundanalysis.dto.FundMetricsDTO;
import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.repository.FundNetValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundMetricsService {

    private final FundNetValueRepository netValueRepository;
    private static final BigDecimal RISK_FREE_RATE = new BigDecimal("0.03");
    private static final int TRADING_DAYS_PER_YEAR = 252;
    private static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    public FundMetricsDTO calculateMetrics(Fund fund) {
        List<FundNetValue> netValues = netValueRepository.findByFundOrderByValueDateAsc(fund);
        
        if (netValues.isEmpty()) {
            log.warn("No net values found for fund: {}", fund.getCode());
            return createEmptyMetrics(fund);
        }

        BigDecimal cumulativeReturn = calculateCumulativeReturn(netValues);
        BigDecimal annualizedReturn = calculateAnnualizedReturn(netValues);
        BigDecimal volatility = calculateVolatility(netValues);
        BigDecimal sharpeRatio = calculateSharpeRatio(annualizedReturn, volatility);
        BigDecimal maxDrawdown = calculateMaxDrawdown(netValues);

        return FundMetricsDTO.builder()
                .fundCode(fund.getCode())
                .fundName(fund.getName())
                .cumulativeReturn(cumulativeReturn)
                .annualizedReturn(annualizedReturn)
                .volatility(volatility)
                .sharpeRatio(sharpeRatio)
                .maxDrawdown(maxDrawdown)
                .dataPoints(netValues.size())
                .build();
    }

    private BigDecimal calculateCumulativeReturn(List<FundNetValue> netValues) {
        if (netValues.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal firstValue = netValues.get(0).getNetValue();
        BigDecimal lastValue = netValues.get(netValues.size() - 1).getNetValue();

        return lastValue.subtract(firstValue)
                .divide(firstValue, MC)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAnnualizedReturn(List<FundNetValue> netValues) {
        if (netValues.size() < 2) {
            return BigDecimal.ZERO;
        }

        BigDecimal firstValue = netValues.get(0).getNetValue();
        BigDecimal lastValue = netValues.get(netValues.size() - 1).getNetValue();
        int days = netValues.size();

        double totalReturn = lastValue.divide(firstValue, MC).doubleValue();
        double yearsExponent = (double) TRADING_DAYS_PER_YEAR / days;
        double annualizedReturn = Math.pow(totalReturn, yearsExponent) - 1;

        return BigDecimal.valueOf(annualizedReturn)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateVolatility(List<FundNetValue> netValues) {
        if (netValues.size() < 2) {
            return BigDecimal.ZERO;
        }

        double[] returns = new double[netValues.size() - 1];
        for (int i = 1; i < netValues.size(); i++) {
            BigDecimal prevValue = netValues.get(i - 1).getNetValue();
            BigDecimal currentValue = netValues.get(i).getNetValue();
            returns[i - 1] = currentValue.subtract(prevValue)
                    .divide(prevValue, MC)
                    .doubleValue();
        }

        double mean = 0;
        for (double ret : returns) {
            mean += ret;
        }
        mean /= returns.length;

        double variance = 0;
        for (double ret : returns) {
            variance += Math.pow(ret - mean, 2);
        }
        variance /= returns.length;

        double dailyVolatility = Math.sqrt(variance);
        double annualizedVolatility = dailyVolatility * Math.sqrt(TRADING_DAYS_PER_YEAR);

        return BigDecimal.valueOf(annualizedVolatility)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSharpeRatio(BigDecimal annualizedReturn, BigDecimal volatility) {
        if (volatility.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal excessReturn = annualizedReturn.divide(new BigDecimal("100"), MC)
                .subtract(RISK_FREE_RATE);
        BigDecimal vol = volatility.divide(new BigDecimal("100"), MC);

        return excessReturn.divide(vol, MC)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMaxDrawdown(List<FundNetValue> netValues) {
        if (netValues.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = netValues.get(0).getNetValue();

        for (FundNetValue netValue : netValues) {
            BigDecimal currentValue = netValue.getNetValue();
            
            if (currentValue.compareTo(peak) > 0) {
                peak = currentValue;
            }

            BigDecimal drawdown = peak.subtract(currentValue)
                    .divide(peak, MC)
                    .multiply(new BigDecimal("100"));

            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown.setScale(2, RoundingMode.HALF_UP);
    }

    private FundMetricsDTO createEmptyMetrics(Fund fund) {
        return FundMetricsDTO.builder()
                .fundCode(fund.getCode())
                .fundName(fund.getName())
                .cumulativeReturn(BigDecimal.ZERO)
                .annualizedReturn(BigDecimal.ZERO)
                .volatility(BigDecimal.ZERO)
                .sharpeRatio(BigDecimal.ZERO)
                .maxDrawdown(BigDecimal.ZERO)
                .dataPoints(0)
                .build();
    }
}
