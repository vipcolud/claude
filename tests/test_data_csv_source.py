import os

from fund_analyzer.data import CSVDataSource


def test_csv_datasource_load():
    base_path = os.path.join(os.path.dirname(__file__), "..", "data")
    ds = CSVDataSource(base_path)
    fund = ds.get_fund("F001")
    assert fund.code == "F001"
    assert fund.name
    assert fund.nav.index.is_monotonic_increasing
    r = fund.returns()
    assert len(r) == len(fund.nav) - 1
