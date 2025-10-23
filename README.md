# 公募基金分析系统 (Fund Analysis System)

基于 **Java + Spring Boot** 的公募基金分析平台，支持从真实基金数据接口自动同步数据，计算核心投资指标，并提供完善的 REST API 供外部系统调用。

## 技术栈

- **Java 17+**
- **Spring Boot 3.2.0**
- **Maven**（项目管理）
- **Spring Data JPA**（持久化层）
- **MySQL 8+**（生产数据库，默认数据源）
- **H2 Database**（测试环境）
- **Lombok**（减少样板代码）
- **JUnit 5 & Mockito & MockMvc**（单元测试／接口测试）

## 核心能力

### 1. 数据模型
- **Fund**：基金基础信息（代码、名称、类型、管理人、描述）
- **FundNetValue**：基金净值历史数据
- **FundType**：基金类型（股票型 / 债券型 / 混合型）

### 2. 指标计算服务（`FundMetricsService`）
- 累计收益率
- 年化收益率
- 波动率（标准差）
- 夏普比率（Sharpe Ratio）
- 最大回撤（Maximum Drawdown）

### 3. 基金分析服务（`FundAnalysisService`）
- 单基金详情与指标
- 多基金对比分析
- 基金筛选与排名（按收益率 / 夏普比率）

### 4. 实时数据同步
- 接入 **DoctorXiong** 开放基金数据接口
- 启动时根据默认基金代码列表自动同步真实数据
- 支持基金信息与净值历史的增量更新

### 5. REST API
- GET `/api/funds`：基金列表
- GET `/api/funds/{code}`：基金详情
- GET `/api/funds/{code}/metrics`：基金指标
- POST `/api/funds/compare`：基金对比
- GET `/api/funds/filter`：基金筛选
- GET `/api/funds/rank/return`：按年化收益率排名
- GET `/api/funds/rank/sharpe`：按夏普比率排名

### 6. 配置与扩展
- `fund.data.enabled`：控制启动时是否自动同步基金数据
- `fund.data.default-codes`：默认同步的基金代码列表
- `fund.data.api-base-url`：外部基金数据 API 基地址（默认 `https://api.doctorxiong.club/v1`）

## 项目结构

```
src/
├── main/
│   ├── java/com/fundanalysis/
│   │   ├── FundAnalysisApplication.java
│   │   ├── config/
│   │   │   ├── DataInitializer.java
│   │   │   ├── FundDataProperties.java
│   │   │   └── RestTemplateConfig.java
│   │   ├── controller/FundController.java
│   │   ├── dto/...
│   │   ├── entity/...
│   │   ├── repository/...
│   │   ├── service/
│   │   │   ├── FundAnalysisService.java
│   │   │   ├── FundDataApiClient.java
│   │   │   ├── FundDataSyncService.java
│   │   │   └── FundMetricsService.java
│   │   └── service/dto/ExternalFundData.java
│   └── resources/
│       ├── application.yml            # MySQL 默认配置
│       └── ...
└── test/
    ├── java/com/fundanalysis/
    │   ├── controller/FundControllerTest.java
    │   ├── service/FundAnalysisServiceTest.java
    │   ├── service/FundDataApiClientTest.java
    │   ├── service/FundDataSyncServiceTest.java
    │   └── service/FundMetricsServiceTest.java
    └── resources/application-test.yml # H2 & 禁用数据同步
```

## 快速开始

### 环境准备
1. 安装 **JDK 17+** 与 **Maven 3.6+**
2. 安装 **MySQL 8+** 并确保服务已启动
3. 创建数据库（如使用默认配置）：
   ```sql
   CREATE DATABASE funddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
4. 配置数据库账号，并在运行前设置以下环境变量（如与默认值不同）：
   ```bash
   export MYSQL_DATASOURCE_URL="jdbc:mysql://localhost:3306/funddb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
   export MYSQL_USERNAME=your_username
   export MYSQL_PASSWORD=your_password
   ```

> **提示**：若暂时无法使用 MySQL，可使用测试配置启动（H2 内存库）：
> ```bash
> mvn spring-boot:run -Dspring-boot.run.profiles=test
> ```
> 测试配置关闭了外部数据同步，适合快速调试。

### 启动应用
```bash
mvn spring-boot:run
```
> 首次启动会访问外部基金数据接口并将默认基金代码的数据同步入库，耗时取决于网络状况。

### 运行测试
```bash
mvn test
```

### 打包发布
```bash
mvn clean package
java -jar target/fund-analysis-system-1.0.0.jar
```

## API 文档

### 1. 查询所有基金
```
GET /api/funds
```

**响应示例**
```json
[
  {
    "code": "000001",
    "name": "华夏成长混合",
    "type": "MIXED",
    "manager": "华夏基金管理有限公司",
    "description": "基金公司：华夏基金管理有限公司，基金规模：123.45亿元，风险等级：中风险"
  }
]
```

### 2. 基金详情
```
GET /api/funds/{code}
```
```json
{
  "code": "000001",
  "name": "华夏成长混合",
  "type": "MIXED",
  "manager": "华夏基金管理有限公司",
  "description": "基金公司：华夏基金管理有限公司，基金规模：123.45亿元，风险等级：中风险",
  "latestNetValue": 1.5234,
  "latestDate": "2024-01-15",
  "totalDataPoints": 600
}
```

### 3. 基金核心指标
```
GET /api/funds/{code}/metrics
```
返回累计收益率、年化收益率、波动率、夏普比率、最大回撤等指标。

### 4. 多基金对比
```
POST /api/funds/compare
Content-Type: application/json
{
  "fundCodes": ["000001", "110022", "163406"]
}
```

### 5. 基金筛选
```
GET /api/funds/filter?type=STOCK&minReturn=20
```
按基金类型与最小年化收益率筛选基金。

### 6. 基金排名
- `GET /api/funds/rank/return`：年化收益率排名
- `GET /api/funds/rank/sharpe`：夏普比率排名

## 数据同步逻辑

- **FundDataApiClient**：封装对外部基金 API 的调用，实现失败重试与异常处理
- **FundDataSyncService**：将外部数据写入本地 MySQL，自动处理基金信息更新与净值数据替换
- **DataInitializer**：应用启动后根据配置同步默认基金代码数据（可通过 `fund.data.enabled=false` 关闭）

## 配置说明

### 默认配置（`src/main/resources/application.yml`）
```yaml
spring:
  datasource:
    url: ${MYSQL_DATASOURCE_URL:jdbc:mysql://localhost:3306/funddb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 2
  jpa:
    database-platform: org.hibernate.dialect.MySQLDialect
    hibernate:
      ddl-auto: update
  jackson:
    default-property-inclusion: non_null

fund:
  data:
    enabled: true
    api-base-url: https://api.doctorxiong.club/v1
    default-codes:
      - 000001
      - 110022
      - 163406
      - 040012
      - 161725
```

### 测试配置（`src/test/resources/application-test.yml`）
- 使用 H2 内存数据库（MySQL 模式）
- 禁用外部数据同步与 CommandLineRunner

## 测试覆盖

| 测试类 | 内容 |
|--------|------|
| `FundMetricsServiceTest` | 指标计算正确性 |
| `FundAnalysisServiceTest` | 业务逻辑与异常处理 |
| `FundControllerTest` | REST API 行为（MockMvc） |
| `FundDataApiClientTest` | 外部接口解析逻辑（MockRestServiceServer） |
| `FundDataSyncServiceTest` | 数据同步与持久化流程 |

## 常见问题（FAQ）

1. **如何禁用启动时的数据同步？**
   在 `application.yml` 或环境变量中设置 `fund.data.enabled=false`。

2. **如何使用 H2 进行本地调试？**
   启动时指定 `test` profile：
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=test
   ```

3. **外部基金接口不可达怎么办？**
   - 启动时会记录警告日志，不会阻止应用继续运行
   - 可手动调用 `FundDataSyncService`（自定义接口）或稍后再次启动

## 开发指南

- 新增基金指标：在 `FundMetricsService` 中计算，在 DTO／Controller 中暴露
- 新增外部数据源：实现新的 API Client，并在 `FundDataSyncService` 中扩展
- 切换目标数据库：修改 `spring.datasource` 配置，即可无缝迁移

## 许可证

本项目基于 **MIT License** 发布。

如需更多功能或反馈问题，请提交 Issue 或 Pull Request。
