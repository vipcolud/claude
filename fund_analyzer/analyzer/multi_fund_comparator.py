from __future__ import annotations

from dataclasses import dataclass
from typing import List, Dict, Optional

import matplotlib.pyplot as plt
import pandas as pd

from ..metrics import annualized_return, volatility, sharpe_ratio, max_drawdown
from ..models import Fund


@dataclass
class FundComparator:
    funds: List[Fund]
    periods_per_year: float = 252.0
    risk_free_rate: float = 0.0

    def metrics_table(self) -> pd.DataFrame:
        rows: Dict[str, Dict[str, float]] = {}
        for f in self.funds:
            r = f.returns()
            rows[f.code] = {
                "name": f.name,
                "annualized_return": annualized_return(r, self.periods_per_year),
                "volatility": volatility(r, self.periods_per_year),
                "sharpe_ratio": sharpe_ratio(r, self.risk_free_rate, self.periods_per_year),
                "max_drawdown": max_drawdown(f.nav),
            }
        df = pd.DataFrame.from_dict(rows, orient="index")
        return df

    def plot_navs(self, ax: Optional[plt.Axes] = None, title: Optional[str] = None) -> plt.Axes:
        if ax is None:
            _, ax = plt.subplots(figsize=(9, 5))
        for f in self.funds:
            f.nav.plot(ax=ax, label=f"{f.name} ({f.code})")
        ax.set_title(title or "NAV Comparison")
        ax.set_xlabel("Date")
        ax.set_ylabel("NAV")
        ax.legend()
        ax.grid(True, alpha=0.3)
        return ax
