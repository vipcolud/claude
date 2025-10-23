from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Optional

import matplotlib.pyplot as plt
import pandas as pd

from ..metrics import (
    annualized_return,
    volatility,
    sharpe_ratio,
    max_drawdown,
    sortino_ratio,
    cumulative_return,
)
from ..models import Fund


@dataclass
class FundAnalyzer:
    fund: Fund
    periods_per_year: float = 252.0
    risk_free_rate: float = 0.0

    def compute_metrics(self) -> Dict[str, float]:
        r = self.fund.returns()
        metrics = {
            "cumulative_return": cumulative_return(r),
            "annualized_return": annualized_return(r, self.periods_per_year),
            "volatility": volatility(r, self.periods_per_year),
            "sharpe_ratio": sharpe_ratio(r, self.risk_free_rate, self.periods_per_year),
            "sortino_ratio": sortino_ratio(r, self.risk_free_rate, self.periods_per_year),
            "max_drawdown": max_drawdown(self.fund.nav),
        }
        return metrics

    def plot_nav(self, ax: Optional[plt.Axes] = None, title: Optional[str] = None) -> plt.Axes:
        if ax is None:
            _, ax = plt.subplots(figsize=(8, 4))
        self.fund.nav.plot(ax=ax, label=self.fund.name)
        ax.set_title(title or f"NAV - {self.fund.name} ({self.fund.code})")
        ax.set_xlabel("Date")
        ax.set_ylabel("NAV")
        ax.legend()
        ax.grid(True, alpha=0.3)
        return ax

    def plot_returns(self, ax: Optional[plt.Axes] = None, title: Optional[str] = None) -> plt.Axes:
        if ax is None:
            _, ax = plt.subplots(figsize=(8, 4))
        r = self.fund.returns()
        r.plot(ax=ax, label=f"{self.fund.name} returns")
        ax.set_title(title or f"Periodic Returns - {self.fund.name} ({self.fund.code})")
        ax.set_xlabel("Date")
        ax.set_ylabel("Return")
        ax.legend()
        ax.grid(True, alpha=0.3)
        return ax

    def summary_dataframe(self) -> pd.DataFrame:
        m = self.compute_metrics()
        df = pd.DataFrame(m, index=[self.fund.code]).T
        df.columns = [self.fund.name]
        return df

    def generate_report(self) -> str:
        info = self.fund.info()
        m = self.compute_metrics()
        lines = [
            f"Fund Report - {info['name']} ({info['code']})",
            f"Type: {info['type']}  Manager: {info['manager']}",
            f"Period: {info['start_date']} -> {info['end_date']}",
            "",
            f"Cumulative Return: {m['cumulative_return']:.2%}",
            f"Annualized Return: {m['annualized_return']:.2%}",
            f"Volatility: {m['volatility']:.2%}",
            f"Sharpe Ratio: {m['sharpe_ratio']:.2f}",
            f"Sortino Ratio: {m['sortino_ratio']:.2f}",
            f"Max Drawdown: {m['max_drawdown']:.2%}",
        ]
        return "\n".join(lines)
