import os
import sys

# Ensure project root is on sys.path so that 'fund_analyzer' is importable when
# tests are executed from within the tests directory by some runners
PROJECT_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if PROJECT_ROOT not in sys.path:
    sys.path.insert(0, PROJECT_ROOT)
