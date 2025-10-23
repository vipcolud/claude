from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable, List, Optional

from ..metrics import annualized_return, volatility, sharpe_ratio, max_drawdown
from ..models import Fund, FundType


@dataclass
class FundFilter:
    funds: Iterable[Fund]
    periods_per_year: float = 252.0
    risk_free_rate: float = 0.0

    def filter(
        self,
        fund_type: Optional[FundType] = None,
        min_annualized_return: Optional[float] = None,
        max_volatility: Optional[float] = None,
        max_drawdown_limit: Optional[float] = None,
        min_sharpe_ratio: Optional[float] = None,
    ) -> List[Fund]:
        selected: List[Fund] = []
        for f in self.funds:
            if fund_type and f.type != fund_type:
                continue
            r = f.returns()
            ann = annualized_return(r, self.periods_per_year)
            vol = volatility(r, self.periods_per_year)
            sr = sharpe_ratio(r, self.risk_free_rate, self.periods_per_year)
            mdd = max_drawdown(f.nav)

            if min_annualized_return is not None and (ann is None or ann < min_annualized_return):
                continue
            if max_volatility is not None and (vol is None or vol > max_volatility):
                continue
            if min_sharpe_ratio is not None and (sr is None or sr < min_sharpe_ratio):
                continue
            if max_drawdown_limit is not None and (mdd is None or mdd < -abs(max_drawdown_limit)):
                # mdd is negative; ensure its absolute value within limit
                continue
            selected.append(f)
        return selected
