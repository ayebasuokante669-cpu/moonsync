from pydantic import BaseModel
from datetime import date
from typing import Literal

NotificationType = Literal[
    "period_start",
    "period_soon",
    "ovulation_today",
    "cycle_irregular"
]

NotificationSource = Literal["system", "ai", "user"]

class Notification(BaseModel):
    date: date
    type: NotificationType
    source: NotificationSource
    confidence: Literal["high", "medium", "low"]
