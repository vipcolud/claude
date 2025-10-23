import math

import numpy as np
import pandas as pd

from fund_analyzer.metrics import (
    annualized_return,
    volatility,
    sharpe_ratio,
    max_drawdown,
    sortino_ratio,
    cumulative_return,
)


def test_cumulative_return_basic():
    r = pd.Series([0.1, -0.05])
    cum = cumulative_return(r)
    assert abs(cum - (1.1 * 0.95 - 1)) < 1e-12


def test_annualized_return_repeat_daily():
    r = pd.Series([0.01] * 252)
    ann = annualized_return(r, periods_per_year=252)
    expected = (1.01 ** 252) - 1
    assert abs(ann - expected) < 1e-12


def test_volatility_zero():
    r = pd.Series([0.0, 0.0, 0.0, 0.0])
    vol = volatility(r, periods_per_year=252)
    assert vol == 0.0


def test_sharpe_ratio_manual():
    r = pd.Series([0.01, 0.02, 0.03])
    rf = 0.0
    sr = sharpe_ratio(r, rf, periods_per_year=3, ddof=0)  # avoid dividing by 0 due to small sample
    # manual computation
    mean = r.mean()
    std = r.std(ddof=0)
    expected = (mean / std) * math.sqrt(3)
    assert abs(sr - expected) < 1e-12


def test_max_drawdown_from_nav():
    nav = pd.Series([1.0, 1.1, 1.05, 1.2, 1.15])
    mdd = max_drawdown(nav)
    assert abs(mdd - (-0.0454545)) < 1e-5


def test_sortino_ratio_defined():
    r = pd.Series([0.01, -0.02, 0.03, -0.01, 0.02])
    sr = sortino_ratio(r, risk_free_rate=0.0, periods_per_year=5)
    assert np.isfinite(sr)
