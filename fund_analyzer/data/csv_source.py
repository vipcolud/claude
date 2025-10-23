from __future__ import annotations

from pathlib import Path
from typing import Optional, Dict

import pandas as pd

from ..models import Fund, FundType
from .datasource import FundDataSource


class CSVDataSource(FundDataSource):
    def __init__(self, base_path: str | Path = "data") -> None:
        self.base_path = Path(base_path)
        self.info_path = self.base_path / "funds_info.csv"
        self.nav_path = self.base_path / "nav"
        if not self.info_path.exists():
            raise FileNotFoundError(f"Missing funds_info.csv at {self.info_path}")
        if not self.nav_path.exists():
            raise FileNotFoundError(f"Missing nav directory at {self.nav_path}")
        self._index: Optional[pd.DataFrame] = None

    def _load_index(self) -> pd.DataFrame:
        if self._index is None:
            df = pd.read_csv(self.info_path)
            # Normalize columns
            cols = {c: c.strip().lower() for c in df.columns}
            df = df.rename(columns=cols)
            required = {"code", "name", "type", "manager"}
            if not required.issubset(df.columns):
                raise ValueError(f"funds_info.csv must contain columns: {required}")
            self._index = df
        return self._index

    def has(self, code: str) -> bool:
        df = self._load_index()
        return (df["code"] == code).any() and (self.nav_path / f"{code}.csv").exists()

    def get_fund(self, code: str) -> Fund:
        df = self._load_index()
        row = df.loc[df["code"] == code]
        if row.empty:
            raise KeyError(f"Fund code {code} not found in index")
        name = str(row.iloc[0]["name"]).strip()
        type_str = str(row.iloc[0]["type"]).strip().lower()
        manager = str(row.iloc[0]["manager"]).strip()
        fund_type = FundType(type_str) if type_str in FundType._value2member_map_ else FundType.OTHER

        nav_file = self.nav_path / f"{code}.csv"
        if not nav_file.exists():
            raise FileNotFoundError(f"NAV file not found: {nav_file}")

        nav_df = pd.read_csv(nav_file)
        cols = {c: c.strip().lower() for c in nav_df.columns}
        nav_df = nav_df.rename(columns=cols)
        if not {"date", "nav"}.issubset(nav_df.columns):
            raise ValueError(f"NAV file must contain 'date' and 'nav' columns: {nav_file}")
        nav_df["date"] = pd.to_datetime(nav_df["date"])  # type: ignore
        nav_df = nav_df.sort_values("date")
        nav_series = pd.Series(nav_df["nav"].values, index=nav_df["date"], name="nav")

        return Fund(code=code, name=name, type=fund_type, manager=manager, nav=nav_series)

    def list_codes(self) -> list[str]:
        df = self._load_index()
        return df["code"].astype(str).tolist()

    def list_funds(self) -> list[Fund]:
        return [self.get_fund(code) for code in self.list_codes() if self.has(code)]
