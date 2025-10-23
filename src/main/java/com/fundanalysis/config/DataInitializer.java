package com.fundanalysis.config;

import com.fundanalysis.entity.Fund;
import com.fundanalysis.entity.FundNetValue;
import com.fundanalysis.entity.FundType;
import com.fundanalysis.repository.FundNetValueRepository;
import com.fundanalysis.repository.FundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FundRepository fundRepository;
    private final FundNetValueRepository netValueRepository;

    @Override
    public void run(String... args) {
        if (fundRepository.count() > 0) {
            log.info("数据已存在，跳过初始化");
            return;
        }

        log.info("开始初始化示例数据...");

        Fund fund1 = createFund(
                "000001",
                "华夏成长混合",
                FundType.MIXED,
                "华夏基金管理有限公司",
                "专注于成长型企业投资的混合型基金，追求长期资本增值"
        );

        Fund fund2 = createFund(
                "110022",
                "易方达消费行业股票",
                FundType.STOCK,
                "易方达基金管理有限公司",
                "主要投资于消费行业优质上市公司，分享消费升级红利"
        );

        Fund fund3 = createFund(
                "163406",
                "兴全可转债混合",
                FundType.MIXED,
                "兴证全球基金管理有限公司",
                "以可转债为主要投资标的的混合型基金，攻守兼备"
        );

        Fund fund4 = createFund(
                "040012",
                "华安强化债券A",
                FundType.BOND,
                "华安基金管理有限公司",
                "以债券投资为主，追求稳健收益的债券型基金"
        );

        Fund fund5 = createFund(
                "161725",
                "招商中证白酒指数",
                FundType.STOCK,
                "招商基金管理有限公司",
                "跟踪中证白酒指数，投资白酒行业龙头企业"
        );

        fundRepository.saveAll(List.of(fund1, fund2, fund3, fund4, fund5));

        generateNetValues(fund1, 1.0, 0.15, 0.02);
        generateNetValues(fund2, 1.0, 0.25, 0.025);
        generateNetValues(fund3, 1.0, 0.12, 0.015);
        generateNetValues(fund4, 1.0, 0.05, 0.008);
        generateNetValues(fund5, 1.0, 0.30, 0.035);

        log.info("数据初始化完成！共创建 {} 个基金", fundRepository.count());
        log.info("共创建 {} 条净值记录", netValueRepository.count());
    }

    private Fund createFund(String code, String name, FundType type, String manager, String description) {
        return new Fund(code, name, type, manager, description);
    }

    private void generateNetValues(Fund fund, double startValue, double annualReturn, double volatility) {
        Random random = new Random(fund.getCode().hashCode());
        List<FundNetValue> netValues = new ArrayList<>();
        
        LocalDate startDate = LocalDate.now().minusYears(2);
        LocalDate endDate = LocalDate.now();
        
        double currentValue = startValue;
        double accumulatedValue = startValue;
        
        double dailyReturn = annualReturn / 252;
        double dailyVolatility = volatility / Math.sqrt(252);
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (date.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            
            double randomReturn = random.nextGaussian() * dailyVolatility + dailyReturn;
            currentValue = currentValue * (1 + randomReturn);
            accumulatedValue = accumulatedValue * (1 + randomReturn);
            
            FundNetValue netValue = new FundNetValue(
                    fund,
                    date,
                    BigDecimal.valueOf(currentValue).setScale(4, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(accumulatedValue).setScale(4, RoundingMode.HALF_UP)
            );
            
            netValues.add(netValue);
        }
        
        netValueRepository.saveAll(netValues);
        log.info("为基金 {} 生成了 {} 条净值记录", fund.getCode(), netValues.size());
    }
}
