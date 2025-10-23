from __future__ import annotations

from typing import Optional

import numpy as np
import pandas as pd


def _to_series(x: pd.Series | pd.DataFrame | np.ndarray | list) -> pd.Series:
    if isinstance(x, pd.Series):
        return x.dropna()
    if isinstance(x, (np.ndarray, list)):
        return pd.Series(x).dropna()
    if isinstance(x, pd.DataFrame):
        if x.shape[1] != 1:
            raise ValueError("DataFrame must have a single column for conversion to Series")
        return x.iloc[:, 0].dropna()
    raise TypeError("Unsupported type for series conversion")


def cumulative_return(returns: pd.Series) -> float:
    r = _to_series(returns)
    if r.empty:
        return np.nan
    return float(np.prod(1 + r) - 1)


def annualized_return(
    returns: pd.Series,
    periods_per_year: float = 252.0,
) -> float:
    r = _to_series(returns)
    if r.empty:
        return np.nan
    n = len(r)
    total_return = np.prod(1 + r)
    if total_return <= 0:
        return np.nan
    ann = total_return ** (periods_per_year / n) - 1
    return float(ann)


def volatility(
    returns: pd.Series,
    periods_per_year: float = 252.0,
    ddof: int = 1,
) -> float:
    r = _to_series(returns)
    if r.empty:
        return np.nan
    vol = r.std(ddof=ddof) * np.sqrt(periods_per_year)
    return float(vol)


def sharpe_ratio(
    returns: pd.Series,
    risk_free_rate: float = 0.0,
    periods_per_year: float = 252.0,
    ddof: int = 1,
) -> float:
    r = _to_series(returns)
    if r.empty:
        return np.nan
    rf_per_period = risk_free_rate / periods_per_year
    excess = r - rf_per_period
    denom = excess.std(ddof=ddof)
    if denom == 0 or np.isnan(denom):
        return np.nan
    sr = (excess.mean() / denom) * np.sqrt(periods_per_year)
    return float(sr)


def sortino_ratio(
    returns: pd.Series,
    risk_free_rate: float = 0.0,
    periods_per_year: float = 252.0,
) -> float:
    r = _to_series(returns)
    if r.empty:
        return np.nan
    rf_per_period = risk_free_rate / periods_per_year
    excess = r - rf_per_period
    downside = excess[excess < 0]
    if downside.empty:
        return np.nan
    downside_std = downside.std(ddof=1)
    if downside_std == 0 or np.isnan(downside_std):
        return np.nan
    sortino = (excess.mean() / downside_std) * np.sqrt(periods_per_year)
    return float(sortino)


def max_drawdown(nav: pd.Series | pd.DataFrame | np.ndarray | list) -> float:
    s = _to_series(nav)
    if s.empty:
        return np.nan
    wealth = s
    # If input looks like returns (centered around 0), convert to wealth
    if (wealth.abs() < 0.3).mean() > 0.8 and ((wealth > -1).all()):
        wealth = (1 + wealth).cumprod()
    running_max = wealth.cummax()
    drawdown = wealth / running_max - 1.0
    return float(drawdown.min())
