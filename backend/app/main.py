from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.exceptions import RequestValidationError
from starlette.exceptions import HTTPException as StarletteHTTPException
from dotenv import load_dotenv
load_dotenv()

from app.api_v1 import api_v1_router
from app.core.errors import (
    http_exception_handler,
    validation_exception_handler,
    generic_exception_handler,
)
from app.ml.safety_layer import handle_prompt

# Routers
from app.cycle.router import router as cycle_router
from app.calendar.router import router as calendar_router
from app.notifications.router import router as notifications_router
from app.ai.router import router as ai_router
from app.history.router import router as history_router
from app.community.router import router as community_router
from app.admin.router import router as admin_router



app = FastAPI()

# Exception handlers
app.add_exception_handler(StarletteHTTPException, http_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, generic_exception_handler)

# Include routers
app.include_router(cycle_router)
app.include_router(calendar_router)
app.include_router(notifications_router)
app.include_router(ai_router)
app.include_router(history_router)
app.include_router(community_router)
app.include_router(admin_router)
app.include_router(api_v1_router)


# Request/Response models
class ChatRequest(BaseModel):
    message: str


class ChatResponse(BaseModel):
    response: str


# Helper functions
def is_greeting(message: str) -> bool:
    greetings = ["hi", "hello", "hey", "sup", "yo", "yolo", "good morning", "good evening"]
    msg_lower = message.lower().strip()
    return any(g in msg_lower for g in greetings) and len(msg_lower.split()) <= 5


def is_casual_chat(message: str) -> bool:
    casual = ["how are you", "how r u", "what's up", "whats up", "how's it going"]
    msg_lower = message.lower().strip()
    return any(c in msg_lower for c in casual)


@app.get("/health")
async def health():
    return {"status": "healthy", "model": "moonsync-v1"}