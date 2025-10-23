package com.fundanalysis.service;

import com.fundanalysis.config.FundDataProperties;
import com.fundanalysis.service.dto.ExternalFundData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FundDataApiClientTest {

    private FundDataApiClient apiClient;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        FundDataProperties properties = new FundDataProperties();
        properties.setApiBaseUrl("http://localhost");
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        apiClient = new FundDataApiClient(restTemplate, properties);
    }

    @Test
    void shouldReturnFundDataWhenResponseIsSuccessful() {
        String json = "{" +
                "\"code\":200," +
                "\"msg\":\"success\"," +
                "\"data\":{" +
                "\"code\":\"000001\"," +
                "\"name\":\"测试基金\"," +
                "\"type\":\"混合型\"," +
                "\"manager\":\"测试管理人\"," +
                "\"netWorthData\":[[\"2023-01-01\",1.234,1.234]]" +
                "}}";

        server.expect(once(), requestTo("http://localhost/fund/detail?code=000001"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<ExternalFundData> result = apiClient.fetchFundDetail("000001");

        server.verify();
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("000001");
        assertThat(result.get().getNetWorthData()).hasSize(1);
    }

    @Test
    void shouldReturnEmptyWhenResponseIsInvalid() {
        String json = "{\"code\":500,\"msg\":\"error\"}";
        server.expect(once(), requestTo("http://localhost/fund/detail?code=000001"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        Optional<ExternalFundData> result = apiClient.fetchFundDetail("000001");

        server.verify();
        assertThat(result).isEmpty();
    }
}
