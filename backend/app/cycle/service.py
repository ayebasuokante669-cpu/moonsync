from datetime import date
from math import floor

FULL_ROTATION = 360.0

DEFAULT_PHASE_ORDER = [
    "period",
    "follicular",
    "ovulation",
    "luteal"
]


PHASES = {
    "period": 0,
    "follicular": 1,
    "ovulation": 2,
    "luteal": 3,
}

DEFAULT_PERIOD_LENGTH = 5
DEFAULT_LUTEAL_LENGTH = 14


def compute_phase_angles(phases: list[dict]) -> list[dict]:
    """
    Takes phases with lengths and returns angle ranges.
    phases = [{ name, length }]
    """
    total_length = sum(p["length"] for p in phases)
    current_angle = 0.0

    result = []

    for phase in phases:
        phase_span = (phase["length"] / total_length) * FULL_ROTATION
        start_angle = current_angle
        end_angle = start_angle + phase_span

        result.append({
            "name": phase["name"],
            "start_angle": round(start_angle, 2),
            "end_angle": round(end_angle, 2),
            "length": phase["length"]
        })

        current_angle = end_angle

    return result


def calculate_cycle_preview(
    last_period_date: date,
    cycle_length: int,
    today: date | None = None,
):
    if today is None:
        today = date.today()

    # days since last period
    days_since = (today - last_period_date).days

    # prevent negative time travel
    if days_since < 0:
        days_since = 0

    cycle_day = (days_since % cycle_length) + 1

    # detect irregular cycles
    is_irregular = cycle_length < 21 or cycle_length > 35

    if is_irregular:
        return {
            "cycle_day": cycle_day,
            "cycle_length": cycle_length,
            "current_phase": "unknown",
            "phase_index": -1,
            "phase_progress": 0.0,
            "days_until_next_period": cycle_length - cycle_day,
            "is_irregular": True,
            "confidence": "low",
        }

    ovulation_day = cycle_length - DEFAULT_LUTEAL_LENGTH

    # determine phase
    if cycle_day <= DEFAULT_PERIOD_LENGTH:
        phase = "period"
        phase_day = cycle_day
        phase_length = DEFAULT_PERIOD_LENGTH

    elif cycle_day < ovulation_day:
        phase = "follicular"
        phase_day = cycle_day - DEFAULT_PERIOD_LENGTH
        phase_length = ovulation_day - DEFAULT_PERIOD_LENGTH - 1

    elif cycle_day == ovulation_day:
        phase = "ovulation"
        phase_day = 1
        phase_length = 1

    else:
        phase = "luteal"
        phase_day = cycle_day - ovulation_day
        phase_length = DEFAULT_LUTEAL_LENGTH

    phase_progress = round(phase_day / phase_length, 4)

    return {
        "cycle_day": cycle_day,
        "cycle_length": cycle_length,
        "current_phase": phase,
        "phase_index": PHASES[phase],
        "phase_progress": phase_progress,
        "days_until_next_period": cycle_length - cycle_day,
        "is_irregular": False,
        "confidence": "high",
    }

def compute_rotation_progress(day_in_cycle: int, cycle_length: int) -> float:
    return round(day_in_cycle / cycle_length, 4)

def build_cycle_geometry(day_in_cycle: int, cycle_length: int, phase_lengths: dict):
    """
    Returns geometry-only data for frontend animation
    """

    phases = [
        {"name": name, "length": phase_lengths[name]}
        for name in DEFAULT_PHASE_ORDER
    ]

    phases_with_angles = compute_phase_angles(phases)
    phases_with_progress, active_phase_index = compute_phase_progress(
        phases_with_angles,
        day_in_cycle
    )

    rotation_progress = compute_rotation_progress(day_in_cycle, cycle_length)

    return {
        "cycle_day": day_in_cycle,
        "cycle_length": cycle_length,
        "rotation_progress": rotation_progress,
        "active_phase_index": active_phase_index,
        "phases": phases_with_progress
    }

def compute_phase_progress(phases_with_angles: list[dict], day_in_cycle: int):
    current_day = 0
    active_phase_index = -1

    for index, phase in enumerate(phases_with_angles):
        phase_length = phase["length"]
        start_day = current_day + 1
        end_day = current_day + phase_length

        if start_day <= day_in_cycle <= end_day:
            active_phase_index = index
            phase_day = day_in_cycle - current_day
            phase_progress = phase_day / phase_length

            phase_span = phase["end_angle"] - phase["start_angle"]
            dot_angle = phase["start_angle"] + (phase_progress * phase_span)

            phase["phase_progress"] = round(phase_progress, 4)
            phase["dot_angle"] = round(dot_angle, 2)
        else:
            phase["phase_progress"] = 0.0
            phase["dot_angle"] = phase["start_angle"]

        current_day = end_day

    return phases_with_angles, active_phase_index


