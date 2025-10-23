package com.fundanalysis.config;

import com.fundanalysis.repository.FundRepository;
import com.fundanalysis.service.FundDataSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final FundDataProperties properties;
    private final FundDataSyncService syncService;
    private final FundRepository fundRepository;

    @Override
    public void run(String... args) {
        if (!properties.isEnabled()) {
            log.info("基金数据同步已禁用，跳过初始化");
            return;
        }

        if (properties.getDefaultCodes() == null || properties.getDefaultCodes().isEmpty()) {
            log.warn("未配置默认基金代码，跳过初始化");
            return;
        }

        log.info("开始同步默认基金数据: {}", properties.getDefaultCodes());
        properties.getDefaultCodes().stream()
                .map(String::trim)
                .filter(code -> !code.isEmpty())
                .forEach(code -> {
                    boolean success = syncService.synchronizeFund(code);
                    if (success) {
                        log.info("基金 {} 同步成功", code);
                    } else {
                        log.warn("基金 {} 同步失败", code);
                    }
                });

        log.info("基金数据初始化完成，当前基金数量：{}", fundRepository.count());
    }
}
