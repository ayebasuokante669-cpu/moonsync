from fastapi import APIRouter, Query
from datetime import date

from app.cycle.service import calculate_cycle_preview
from app.calendar.service import build_calendar_preview
from app.notifications.service import generate_notifications

router = APIRouter(prefix="/notifications", tags=["notifications"])


@router.get("/ping")
def ping():
    return {"status": "notifications alive"}


@router.get("/preview")
def preview_notifications(
    last_period_date: date = Query(...),
    cycle_length: int = Query(...),
):
    today = date.today()

    cycle_preview = calculate_cycle_preview(
        last_period_date=last_period_date,
        cycle_length=cycle_length,
        today=today,
    )

    calendar = build_calendar_preview(
        last_period_date=last_period_date,
        cycle_length=cycle_length,
    )

    notifications = generate_notifications(
        today=today,
        cycle_preview=cycle_preview,
        future_events=calendar["events"],
    )

    return {
        "cycle": cycle_preview,
        "notifications": notifications,
    }
