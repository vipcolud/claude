package com.fundanalysis.service;

import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.repository.FundNetValueRepository;
import com.fundanalysis.repository.FundRepository;
import com.fundanalysis.service.dto.ExternalFundData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundDataSyncService {

    private final FundDataApiClient apiClient;
    private final FundRepository fundRepository;
    private final FundNetValueRepository netValueRepository;

    @Transactional
    public boolean synchronizeFund(String fundCode) {
        if (fundCode == null || fundCode.isBlank()) {
            return false;
        }
        String normalizedCode = fundCode.trim();
        Optional<ExternalFundData> externalFundOpt = apiClient.fetchFundDetail(normalizedCode);
        if (externalFundOpt.isEmpty()) {
            log.warn("未能从外部接口获取基金 {} 的数据", normalizedCode);
            return false;
        }

        ExternalFundData externalFund = externalFundOpt.get();
        Fund fund = fundRepository.findByCode(normalizedCode)
                .orElseGet(Fund::new);

        fund.setCode(normalizedCode);
        fund.setName(Objects.requireNonNullElse(externalFund.getName(), normalizedCode));
        fund.setManager(Objects.requireNonNullElse(externalFund.getManager(), "未知管理人"));
        fund.setType(resolveFundType(externalFund.getType()));
        fund.setDescription(resolveDescription(externalFund));

        Fund saved = fundRepository.save(fund);

        List<FundNetValue> netValues = convertNetValues(saved, externalFund);
        if (netValues.isEmpty()) {
            log.warn("基金 {} 未获取到有效的净值数据", normalizedCode);
            return true;
        }

        saved.getNetValues().clear();
        netValueRepository.deleteByFund(saved);
        netValueRepository.saveAll(netValues);
        log.info("基金 {} 数据同步完成，共保存 {} 条净值记录", normalizedCode, netValues.size());
        return true;
    }

    private List<FundNetValue> convertNetValues(Fund fund, ExternalFundData data) {
        List<List<Object>> source = data.getNetWorthData();
        if (source == null || source.isEmpty()) {
            source = data.getNetValueData();
        }
        if (source == null) {
            return List.of();
        }

        List<FundNetValue> results = new ArrayList<>();
        for (List<Object> row : source) {
            if (row == null || row.size() < 2) {
                continue;
            }
            try {
                LocalDate date = LocalDate.parse(String.valueOf(row.get(0)));
                BigDecimal netValue = new BigDecimal(String.valueOf(row.get(1)));
                BigDecimal accumulated = row.size() > 2 ?
                        new BigDecimal(String.valueOf(row.get(2))) : netValue;
                FundNetValue entity = new FundNetValue();
                entity.setFund(fund);
                entity.setValueDate(date);
                entity.setNetValue(netValue);
                entity.setAccumulatedValue(accumulated);
                results.add(entity);
            } catch (DateTimeParseException | NumberFormatException ex) {
                log.debug("解析基金 {} 的净值数据失败，原始数据: {}", fund.getCode(), row, ex);
            }
        }
        results.sort(java.util.Comparator.comparing(FundNetValue::getValueDate));
        return results;
    }

    private String resolveDescription(ExternalFundData data) {
        List<String> segments = new ArrayList<>();
        if (data.getFundCompany() != null && !data.getFundCompany().isBlank()) {
            segments.add("基金公司：" + data.getFundCompany());
        }
        if (data.getFundScale() != null && !data.getFundScale().isBlank()) {
            segments.add("基金规模：" + data.getFundScale());
        }
        if (data.getFundRiskLevel() != null && !data.getFundRiskLevel().isBlank()) {
            segments.add("风险等级：" + data.getFundRiskLevel());
        }
        if (segments.isEmpty()) {
            return "来自开放API的基金数据";
        }
        return String.join("，", segments);
    }

    private FundType resolveFundType(String typeText) {
        if (typeText == null || typeText.isBlank()) {
            return FundType.MIXED;
        }
        String text = typeText.toLowerCase(Locale.ROOT);
        if (text.contains("bond") || typeText.contains("债")) {
            return FundType.BOND;
        }
        if (text.contains("stock") || typeText.contains("股")) {
            return FundType.STOCK;
        }
        if (text.contains("mix") || text.contains("balanced") || typeText.contains("混")) {
            return FundType.MIXED;
        }
        return FundType.MIXED;
    }
}
