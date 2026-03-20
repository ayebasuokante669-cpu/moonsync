# app/issues/schemas.py

from pydantic import BaseModel
from uuid import UUID
from datetime import datetime
from typing import Optional


class IssueCreate(BaseModel):
    title: str
    description: str
    category: Optional[str] = "general"


class IssueUpdate(BaseModel):
    status: str  # open, in_progress, resolved
    admin_note: Optional[str] = None


class IssueResponse(BaseModel):
    id: UUID
    user_id: UUID
    title: str
    description: str
    category: str
    status: str
    created_at: datetime