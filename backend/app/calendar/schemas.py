from datetime import date
from pydantic import BaseModel
from typing import Literal


EventSource = Literal["system", "user", "ai"]
EventType = Literal["period", "ovulation", "fertile_window", "note"]


class CalendarEvent(BaseModel):
    date: date
    type: EventType
    source: EventSource
    confidence: Literal["high", "medium", "low"]
