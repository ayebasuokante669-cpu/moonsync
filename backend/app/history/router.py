
from fastapi import APIRouter, Depends
from app.history.schemas import HistoryResponse
from app.history.service import get_user_history

router = APIRouter(prefix="/history", tags=["history"])


@router.get("/{user_id}", response_model=HistoryResponse)
def fetch_user_history(user_id: str):
    """
    Returns the full Moon Archive (History) for a user.

    This endpoint:
    - Aggregates symptoms, moods, notes, and cycle data
    - Is read-only
    - Powers the History screen in the app
    """
    return get_user_history(user_id)
