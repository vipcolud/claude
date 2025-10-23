# 公募基金分析框架（Python）

一个用于分析公募基金（股票、债券、混合等）的轻量级 Python 框架，涵盖数据模型、核心绩效指标计算、分析器以及可视化能力。

## 功能概览
- 基金数据模型：代码、名称、类型、管理人、净值序列
- 指标计算：年化收益率、最大回撤、夏普比率、波动率、Sortino 等
- 分析器：
  - 单基金分析报告与图表
  - 多基金对比与筛选
- 数据接口：CSV 数据源，预留 API 接口扩展
- 可视化：净值走势图、收益率图、对比图

## 目录结构
```
fund-analysis-framework/
├── fund_analyzer/
│   ├── models/          # 数据模型
│   ├── metrics/         # 指标计算
│   ├── analyzer/        # 分析器核心
│   └── data/            # 数据接口
├── examples/            # 使用示例
├── tests/               # 单元测试
├── data/                # 示例数据
├── requirements.txt     # 依赖
└── README.md            # 使用文档
```

## 快速开始
1. 安装依赖
```
pip install -r requirements.txt
```

2. 运行示例
- 单基金分析
```
python examples/example_single_fund_analysis.py
```
将输出文字报告，并生成图片：
- single_fund_nav.png
- single_fund_returns.png

- 多基金对比
```
python examples/example_multi_fund_compare.py
```
将输出对比表，并生成图片：
- multi_fund_navs.png

## 核心 API 说明
- 数据模型：`fund_analyzer.models.Fund`
- 数据源：`fund_analyzer.data.CSVDataSource`
- 指标：`fund_analyzer.metrics` 模块
- 分析器：
  - `fund_analyzer.analyzer.FundAnalyzer`（单基金）
  - `fund_analyzer.analyzer.FundComparator`（多基金对比）
  - `fund_analyzer.analyzer.FundFilter`（筛选）

### CSV 数据格式
- `data/funds_info.csv`
```
code,name,type,manager
F001,Alpha Equity Fund,equity,Alpha Asset Mgmt
F002,Beta Bond Fund,bond,Beta Capital
```

- `data/nav/<code>.csv`
```
date,nav
2023-01-02,1.000
2023-01-05,1.010
...
```

## 单元测试
```
pytest -q
```

## 扩展（API 接口）
`fund_analyzer.data.FundDataSource` 定义了数据源抽象，可实现自定义数据源（如 HTTP API、数据库等），只需实现 `get_fund` 与 `has` 方法。

## 许可
MIT
