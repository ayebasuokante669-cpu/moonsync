from datetime import date, timedelta
from app.cycle.service import DEFAULT_LUTEAL_LENGTH


def generate_cycle_events(
    last_period_date: date,
    cycle_length: int,
    days_ahead: int = 90,
    is_irregular: bool = False,
):
    events = []
    confidence = "low" if is_irregular else "high"

    current_date = last_period_date
    end_date = last_period_date + timedelta(days=days_ahead)

    while current_date <= end_date:
        # Period start
        events.append({
            "date": current_date,
            "type": "period",
            "source": "system",
            "confidence": confidence,
        })

        # Ovulation (if regular)
        if not is_irregular:
            ovulation_date = current_date + timedelta(
                days=cycle_length - DEFAULT_LUTEAL_LENGTH
            )
            events.append({
                "date": ovulation_date,
                "type": "ovulation",
                "source": "system",
                "confidence": confidence,
            })

        current_date += timedelta(days=cycle_length)

    return events

def build_calendar_preview(
    last_period_date: date,
    cycle_length: int,
    days_ahead: int = 90,
):
    is_irregular = cycle_length < 21 or cycle_length > 35

    events = generate_cycle_events(
        last_period_date=last_period_date,
        cycle_length=cycle_length,
        days_ahead=days_ahead,
        is_irregular=is_irregular,
    )

    return {
        "events": events,
        "confidence": "low" if is_irregular else "high",
    }

