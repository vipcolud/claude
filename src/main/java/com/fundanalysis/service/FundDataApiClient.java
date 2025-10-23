package com.fundanalysis.service;

import com.fundanalysis.config.FundDataProperties;
import com.fundanalysis.service.dto.ExternalFundData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FundDataApiClient {

    private final RestTemplate restTemplate;
    private final FundDataProperties properties;

    private static final String FUND_DETAIL_PATH = "/fund/detail";

    public Optional<ExternalFundData> fetchFundDetail(String fundCode) {
        String url = String.format("%s%s?code=%s", properties.getApiBaseUrl(), FUND_DETAIL_PATH, fundCode);
        try {
            ResponseEntity<FundDetailResponse> response = restTemplate.getForEntity(url, FundDetailResponse.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("获取基金 {} 数据失败，HTTP状态码: {}", fundCode, response.getStatusCode());
                return Optional.empty();
            }

            FundDetailResponse body = response.getBody();
            if (body.getCode() != 200 || body.getData() == null) {
                log.warn("基金 {} 数据接口返回异常，code: {}, message: {}", fundCode, body.getCode(), body.getMsg());
                return Optional.empty();
            }

            return Optional.of(body.getData());
        } catch (RestClientException ex) {
            log.error("调用基金数据接口异常，基金代码: {}", fundCode, ex);
            return Optional.empty();
        }
    }

    @lombok.Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private static class FundDetailResponse {
        private int code;
        private String msg;
        private ExternalFundData data;
    }
}
