from fastapi import APIRouter, Query
from datetime import date
from app.cycle.schemas import CyclePreviewResponse
from app.cycle.service import calculate_cycle_preview

router = APIRouter(prefix="/cycle", tags=["Cycle"])


@router.get("/preview", response_model=CyclePreviewResponse)
def preview_cycle(
    last_period_date: date = Query(...),
    cycle_length: int = Query(...),
):
    return calculate_cycle_preview(
        last_period_date=last_period_date,
        cycle_length=cycle_length,
    )

    geometry = build_cycle_geometry(
    day_in_cycle=day_in_cycle,
    cycle_length=cycle_length,
    phase_lengths=phase_lengths
)

