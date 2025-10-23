from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Optional

from ..models import Fund


class FundDataSource(ABC):
    @abstractmethod
    def get_fund(self, code: str) -> Fund:
        raise NotImplementedError

    @abstractmethod
    def has(self, code: str) -> bool:
        raise NotImplementedError

    def try_get(self, code: str) -> Optional[Fund]:
        return self.get_fund(code) if self.has(code) else None
