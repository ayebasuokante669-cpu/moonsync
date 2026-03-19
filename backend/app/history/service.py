

"""
This file handles:
- Fetching user history data from the database
- Aggregating multiple tables into a single timeline
- Preparing data exactly as the History screen needs
"""

from datetime import date
from typing import List
from app.history.schemas import (
    HistoryResponse,
    HistorySummary,
    DailyHistoryEntry,
    SymptomEntry,
    MoodEntry
)

# NOTE:
# This is a placeholder implementation.
# The DB person will later replace this with real SQL/Supabase queries.


def get_user_history(user_id: str) -> HistoryResponse:
    """
    Collects and merges all historical data for a user.
    This includes:
    - Symptoms
    - Moods
    - Notes
    - Cycle info

    The chatbot does NOT automatically write here.
    Only explicitly logged data appears in history.
    """

    # --------------------
    # MOCK DATA (temporary)
    # --------------------

    summary = HistorySummary(
        days_logged=1,
        average_cycle_length=28,
        average_period_length=5,
        tracking_since=date(2026, 2, 1)
    )

    entries: List[DailyHistoryEntry] = [
        DailyHistoryEntry(
            date=date(2026, 2, 7),
            physical_symptoms=[
                SymptomEntry(symptom_name="Cramps", severity=3),
                SymptomEntry(symptom_name="Headache", severity=2)
            ],
            emotional_states=[
                MoodEntry(mood_name="Irritable", intensity=3),
                MoodEntry(mood_name="Sad", intensity=2),
                MoodEntry(mood_name="Happy", intensity=1)
            ],
            notes="Heavy bleeding today",
            logged_at=date(2026, 2, 7)
        )
    ]

    return HistoryResponse(
        summary=summary,
        entries=entries
    )
