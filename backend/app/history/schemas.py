from pydantic import BaseModel
from datetime import date, datetime
from typing import List, Optional

#This represents one sypmtom logged on a day
class SymptomEntry(BaseModel):
    symptom_name: str
    severity: Optional[int] = None  # Severity on a scale of 1-10, optional
    notes: Optional[str] = None  # Additional notes about the symptom, optional

#This represents one mood logged on a day
class MoodEntry(BaseModel):
    mood_name: str
    intensity: Optional[int] = None  # Intensity on a scale of 1-10, optional
    notes: Optional[str] = None  # Additional notes about the mood, optional

#Represents one full day in the history, with all symptoms and moods logged
class DailyHistoryEntry(BaseModel):
    date: date
    physical_symptoms: List[SymptomEntry] 
    emotional_states: List[MoodEntry] 
    notes: Optional[str] = None  # Additional notes about the day, optional
    logged_at: datetime 

#Summary card at the the top of the screen
class HistorySummary(BaseModel):
    days_logged: int
    average_cycle_length: Optional[int]
    average_period_length: Optional[int]
    tracking_since: date

#Final response returned to the frontend, containing the summary and the full history
class HistoryResponse(BaseModel):
    summary: HistorySummary
    entries: List[DailyHistoryEntry]