# app/ml/lora/router.py

from fastapi import APIRouter
from app.ai.llm_service import generate_reply

router = APIRouter(prefix="/ai")

@router.post("/chat")
async def chat(message: str):
    response = generate_reply(message)
    return response