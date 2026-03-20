from fastapi import APIRouter, Query
from datetime import date
from app.calendar.service import generate_cycle_events
from app.cycle.service import calculate_cycle_preview

router = APIRouter(prefix="/calendar", tags=["calendar"])


@router.get("/preview")
def calendar_preview(
    last_period_date: date = Query(...),
    cycle_length: int = Query(...),
):
    cycle = calculate_cycle_preview(
        last_period_date=last_period_date,
        cycle_length=cycle_length
    )

    events = generate_cycle_events(
        last_period_date=last_period_date,
        cycle_length=cycle_length,
        is_irregular=cycle["is_irregular"]
    )

    return {
        "cycle": cycle,
        "events": events
    }


@router.get("/ping")
def calendar_ping():
    return {"status": "calendar alive"}
