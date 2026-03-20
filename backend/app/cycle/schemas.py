from pydantic import BaseModel, Field
from datetime import date
from typing import Optional


class CyclePreviewRequest(BaseModel):
    last_period_date: date
    cycle_length: int = Field(..., ge=15, le=60)  # allow detection of irregular cycles
    today: Optional[date] = None


class CyclePreviewResponse(BaseModel):
    cycle_day: int
    cycle_length: int
    current_phase: str
    phase_index: int
    phase_progress: float
    days_until_next_period: int
    is_irregular: bool
    confidence: str
