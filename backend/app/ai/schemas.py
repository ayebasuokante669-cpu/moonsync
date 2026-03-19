from pydantic import BaseModel, Field
from typing import Optional

class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=1000)
    session_id: Optional[str] = None

class ChatResponse(BaseModel):
    response: str
    confidence: str
    session_id: str
    is_generating: bool = False ##frontend loading state

class VoiceInputResponse(BaseModel):
    text: str
    session_id: Optional[str] = None

class VoiceResponseRequest(BaseModel):
    text: str
