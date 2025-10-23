# 公募基金分析系统 (Fund Analysis System)

基于 Java + Spring Boot 的公募基金分析系统，用于分析股票型、债券型、混合型公募基金的各项财务指标。

## 技术栈

- **Java 17+**
- **Spring Boot 3.2.0**
- **Maven** - 项目管理
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 内存数据库（开发测试用）
- **Lombok** - 简化代码
- **JUnit 5** - 单元测试

## 核心功能

### 1. 基金数据模型
- **Fund** - 基金实体（代码、名称、类型、管理人）
- **FundNetValue** - 净值历史记录
- **FundType** - 基金类型枚举（股票型、债券型、混合型）

### 2. 基金指标计算
- 累计收益率
- 年化收益率
- 波动率（标准差）
- 夏普比率 (Sharpe Ratio)
- 最大回撤 (Maximum Drawdown)

### 3. 基金分析服务
- 单基金详细分析
- 多基金对比分析
- 基金排名和筛选

## 项目结构

```
fund-analysis-system/
├── src/
│   ├── main/
│   │   ├── java/com/fundanalysis/
│   │   │   ├── FundAnalysisApplication.java    # 主应用程序
│   │   │   ├── entity/                          # 实体类
│   │   │   │   ├── Fund.java
│   │   │   │   ├── FundNetValue.java
│   │   │   │   └── FundType.java
│   │   │   ├── repository/                      # JPA 仓储
│   │   │   │   ├── FundRepository.java
│   │   │   │   └── FundNetValueRepository.java
│   │   │   ├── service/                         # 业务逻辑
│   │   │   │   ├── FundMetricsService.java
│   │   │   │   └── FundAnalysisService.java
│   │   │   ├── controller/                      # REST 控制器
│   │   │   │   └── FundController.java
│   │   │   ├── dto/                             # 数据传输对象
│   │   │   └── config/                          # 配置类
│   │   │       └── DataInitializer.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/                                     # 单元测试
│       └── java/com/fundanalysis/
│           ├── service/
│           └── controller/
├── pom.xml
└── README.md
```

## 快速开始

### 前置要求
- JDK 17 或更高版本
- Maven 3.6+

### 启动应用

1. **克隆或进入项目目录**
```bash
cd fund-analysis-system
```

2. **使用 Maven 启动应用**
```bash
mvn spring-boot:run
```

3. **访问应用**
- API 端点: http://localhost:8080/api/funds
- H2 控制台: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:funddb`
  - Username: `sa`
  - Password: (留空)

### 运行测试
```bash
mvn test
```

### 打包应用
```bash
mvn clean package
java -jar target/fund-analysis-system-1.0.0.jar
```

## API 文档

### 1. 查询所有基金
```http
GET /api/funds
```

**响应示例:**
```json
[
  {
    "code": "000001",
    "name": "华夏成长混合",
    "type": "MIXED",
    "manager": "华夏基金管理有限公司",
    "description": "专注于成长型企业投资的混合型基金"
  }
]
```

### 2. 查询基金详情
```http
GET /api/funds/{code}
```

**参数:**
- `code` - 基金代码

**响应示例:**
```json
{
  "code": "000001",
  "name": "华夏成长混合",
  "type": "MIXED",
  "manager": "华夏基金管理有限公司",
  "description": "专注于成长型企业投资的混合型基金",
  "latestNetValue": 1.5234,
  "latestDate": "2024-01-15",
  "totalDataPoints": 500
}
```

### 3. 获取基金指标
```http
GET /api/funds/{code}/metrics
```

**响应示例:**
```json
{
  "fundCode": "000001",
  "fundName": "华夏成长混合",
  "cumulativeReturn": 52.34,
  "annualizedReturn": 23.45,
  "volatility": 18.56,
  "sharpeRatio": 1.12,
  "maxDrawdown": 15.67,
  "dataPoints": 500
}
```

**指标说明:**
- `cumulativeReturn` - 累计收益率 (%)
- `annualizedReturn` - 年化收益率 (%)
- `volatility` - 年化波动率 (%)
- `sharpeRatio` - 夏普比率（越高越好）
- `maxDrawdown` - 最大回撤 (%)
- `dataPoints` - 数据点数量

### 4. 对比多个基金
```http
POST /api/funds/compare
Content-Type: application/json

{
  "fundCodes": ["000001", "110022", "163406"]
}
```

**响应示例:**
```json
{
  "funds": [
    {
      "fundCode": "000001",
      "fundName": "华夏成长混合",
      "cumulativeReturn": 52.34,
      "annualizedReturn": 23.45,
      "volatility": 18.56,
      "sharpeRatio": 1.12,
      "maxDrawdown": 15.67
    },
    {
      "fundCode": "110022",
      "fundName": "易方达消费行业股票",
      "cumulativeReturn": 68.90,
      "annualizedReturn": 28.12,
      "volatility": 22.34,
      "sharpeRatio": 1.08,
      "maxDrawdown": 20.45
    }
  ]
}
```

### 5. 筛选基金
```http
GET /api/funds/filter?type=STOCK&minReturn=20
```

**查询参数:**
- `type` (可选) - 基金类型: `STOCK`, `BOND`, `MIXED`
- `minReturn` (可选) - 最小年化收益率

**响应示例:**
```json
[
  {
    "code": "110022",
    "name": "易方达消费行业股票",
    "type": "STOCK",
    "manager": "易方达基金管理有限公司"
  }
]
```

### 6. 按收益率排名
```http
GET /api/funds/rank/return
```

返回所有基金按年化收益率降序排列。

### 7. 按夏普比率排名
```http
GET /api/funds/rank/sharpe
```

返回所有基金按夏普比率降序排列。

## 示例数据

系统启动时会自动初始化以下示例基金:

| 代码 | 名称 | 类型 | 管理人 |
|------|------|------|--------|
| 000001 | 华夏成长混合 | 混合型 | 华夏基金管理有限公司 |
| 110022 | 易方达消费行业股票 | 股票型 | 易方达基金管理有限公司 |
| 163406 | 兴全可转债混合 | 混合型 | 兴证全球基金管理有限公司 |
| 040012 | 华安强化债券A | 债券型 | 华安基金管理有限公司 |
| 161725 | 招商中证白酒指数 | 股票型 | 招商基金管理有限公司 |

每个基金包含约 500 条净值历史记录（近 2 年工作日数据）。

## 配置说明

### application.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:funddb      # H2 内存数据库
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop       # 启动时创建表，关闭时删除
    show-sql: false

server:
  port: 8080                      # 服务端口
```

## 技术实现细节

### 夏普比率计算
```
Sharpe Ratio = (年化收益率 - 无风险利率) / 波动率
```
- 无风险利率假设为 3%
- 年化交易日为 252 天

### 最大回撤计算
最大回撤表示从历史最高点到最低点的最大跌幅百分比。

### 波动率计算
使用日收益率的标准差，并年化处理（乘以 √252）。

## 开发说明

### 添加新的基金指标

1. 在 `FundMetricsService` 中添加计算方法
2. 在 `FundMetricsDTO` 中添加对应字段
3. 更新控制器和文档

### 切换到生产数据库

修改 `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/funddb
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

添加 MySQL 依赖到 `pom.xml`:
```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
</dependency>
```

## 许可证

MIT License

## 联系方式

如有问题或建议，请提交 Issue。
