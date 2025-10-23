from __future__ import annotations

from dataclasses import dataclass
from enum import Enum
from typing import Optional, Dict, Any

import pandas as pd


class FundType(str, Enum):
    EQUITY = "equity"
    BOND = "bond"
    MIXED = "mixed"
    OTHER = "other"


@dataclass
class Fund:
    code: str
    name: str
    type: FundType
    manager: Optional[str]
    nav: pd.Series  # Indexed by datetime64[ns], values represent unit net asset value
    extra: Optional[Dict[str, Any]] = None

    def __post_init__(self) -> None:
        if not isinstance(self.nav, pd.Series):
            raise TypeError("nav must be a pandas Series")
        if self.nav.empty:
            raise ValueError("nav series is empty")
        if self.nav.index.inferred_type not in {"datetime64", "datetime"}:
            # try to convert
            self.nav.index = pd.to_datetime(self.nav.index)
        # Ensure sorted by date ascending
        self.nav = self.nav.sort_index()

    @property
    def start_date(self) -> pd.Timestamp:
        return pd.Timestamp(self.nav.index[0])

    @property
    def end_date(self) -> pd.Timestamp:
        return pd.Timestamp(self.nav.index[-1])

    def returns(self) -> pd.Series:
        """Compute periodic returns from NAV series using pct_change.

        Returns are aligned to the NAV frequency and start from the second observation.
        """
        r = self.nav.pct_change().dropna()
        r.name = f"{self.code}_returns"
        return r

    def as_dataframe(self) -> pd.DataFrame:
        df = pd.DataFrame({"nav": self.nav})
        df.index.name = "date"
        return df

    def info(self) -> Dict[str, Any]:
        return {
            "code": self.code,
            "name": self.name,
            "type": self.type.value,
            "manager": self.manager,
            "start_date": self.start_date.date().isoformat(),
            "end_date": self.end_date.date().isoformat(),
        }
