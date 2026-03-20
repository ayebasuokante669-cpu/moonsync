# app/safety/router.py

from fastapi import APIRouter
from app.safety.rules import PROHIBITED_CONTENT

router = APIRouter(prefix="/safety", tags=["Safety"])


@router.get("/rules")
def get_rules():
    return {"rules": PROHIBITED_CONTENT}