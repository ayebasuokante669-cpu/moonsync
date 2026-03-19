"""
activity_logger.py

Centralized logging utility for MoonSync.

This file handles:
- Logging user activities
- Logging admin actions
- Tracking system-level events

Why this exists:
Instead of writing logging logic inside every router or service,
we centralize it here for consistency and maintainability.
"""

from datetime import datetime
from uuid import uuid4
from app.database import get_db


async def log_user_activity(
    db,
    user_id,
    event_type: str,
    source_table: str,
    source_id,
    title: str,
    description: str,
    severity: str = None
):
    """
    Inserts a new record into the user_history table.

    Parameters:
    - db: Database session
    - user_id: ID of the user performing the action
    - event_type: Type of event (e.g., 'cycle_log', 'chat', 'issue_report')
    - source_table: Table where original data came from
    - source_id: ID of original record
    - title: Short title for history
    - description: Full description
    - severity: Optional severity level
    """

    await db.execute(
        """
        INSERT INTO user_history (
            id,
            user_id,
            event_type,
            source_table,
            source_id,
            title,
            description,
            severity,
            event_date,
            created_at
        )
        VALUES (
            :id,
            :user_id,
            :event_type,
            :source_table,
            :source_id,
            :title,
            :description,
            :severity,
            :event_date,
            :created_at
        )
        """,
        {
            "id": str(uuid4()),
            "user_id": user_id,
            "event_type": event_type,
            "source_table": source_table,
            "source_id": source_id,
            "title": title,
            "description": description,
            "severity": severity,
            "event_date": datetime.utcnow(),
            "created_at": datetime.utcnow(),
        },
    )

    await db.commit()