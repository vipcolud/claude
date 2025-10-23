import os

from fund_analyzer.analyzer import FundAnalyzer
from fund_analyzer.data import CSVDataSource


def main():
    base_path = os.path.join(os.path.dirname(__file__), "..", "data")
    ds = CSVDataSource(base_path)
    fund = ds.get_fund("F001")

    analyzer = FundAnalyzer(fund)
    print(analyzer.generate_report())

    # Plot NAV and returns
    ax1 = analyzer.plot_nav()
    fig1 = ax1.get_figure()
    fig1.tight_layout()
    fig1.savefig("single_fund_nav.png")

    ax2 = analyzer.plot_returns()
    fig2 = ax2.get_figure()
    fig2.tight_layout()
    fig2.savefig("single_fund_returns.png")


if __name__ == "__main__":
    main()
