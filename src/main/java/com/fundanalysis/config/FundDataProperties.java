package com.fundanalysis.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fund.data")
public class FundDataProperties {

    private boolean enabled = true;
    private String apiBaseUrl = "https://api.doctorxiong.club/v1";
    private List<String> defaultCodes = new ArrayList<>(List.of(
            "000001",
            "110022",
            "163406",
            "040012",
            "161725"
    ));
}
