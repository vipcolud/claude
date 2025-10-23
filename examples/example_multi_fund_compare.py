import os
import sys
from pathlib import Path

# Ensure project root is on sys.path for local execution
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from fund_analyzer.analyzer import FundComparator, FundFilter
from fund_analyzer.data import CSVDataSource
from fund_analyzer.models import FundType


def main():
    base_path = os.path.join(os.path.dirname(__file__), "..", "data")
    ds = CSVDataSource(base_path)
    funds = [ds.get_fund(code) for code in ds.list_codes()]

    comparator = FundComparator(funds)
    table = comparator.metrics_table()
    print("Metrics Table:\n", table)

    # Plot comparison chart
    ax = comparator.plot_navs()
    fig = ax.get_figure()
    fig.tight_layout()
    fig.savefig("multi_fund_navs.png")

    # Filter funds by type and metrics
    ffilter = FundFilter(funds)
    selected = ffilter.filter(
        fund_type=FundType.EQUITY,
        min_annualized_return=-0.1,  # loose filter for example
        max_volatility=1.0,
        min_sharpe_ratio=-10,
        max_drawdown_limit=0.9,
    )
    print("Selected funds:")
    for f in selected:
        print(f"- {f.code} {f.name} ({f.type.value})")


if __name__ == "__main__":
    main()
