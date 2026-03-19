from datetime import date, timedelta
from typing import List

from app.notifications.schemas import Notification


def generate_notifications(
    *,
    today: date,
    cycle_preview: dict,
    future_events: list[dict],
) -> List[Notification]:

    notifications: List[Notification] = []

    # 1️⃣ Irregular cycle warning
    if cycle_preview.get("is_irregular"):
        notifications.append(
            Notification(
                date=today,
                type="cycle_irregular",
                source="system",
                confidence="low",
            )
        )
        return notifications  # stop here — low confidence system

    days_until_period = cycle_preview["days_until_next_period"]
    current_phase = cycle_preview["current_phase"]

    # 2️⃣ Period starting soon (2 days before)
    if days_until_period == 2:
        notifications.append(
            Notification(
                date=today,
                type="period_soon",
                source="system",
                confidence="high",
            )
        )

    # 3️⃣ Period starts today
    if days_until_period == 0:
        notifications.append(
            Notification(
                date=today,
                type="period_start",
                source="system",
                confidence="high",
            )
        )

    # 4️⃣ Ovulation today (from calendar events)
    for event in future_events:
        if (
            event["type"] == "ovulation"
            and event["date"] == today.isoformat()
        ):
            notifications.append(
                Notification(
                    date=today,
                    type="ovulation_today",
                    source="system",
                    confidence="high",
                )
            )

    return notifications
