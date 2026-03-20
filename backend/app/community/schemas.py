"""
Community Schemas

Pydantic models for request/response validation.
"""

from pydantic import BaseModel
from typing import Optional
from uuid import UUID
from datetime import datetime


class PostCreate(BaseModel):
    content: str


class PostResponse(BaseModel):
    id: UUID
    user_id: UUID
    content: str
    created_at: datetime


class CommentCreate(BaseModel):
    post_id: UUID
    content: str


class CommentResponse(BaseModel):
    id: UUID
    post_id: UUID
    user_id: UUID
    content: str
    created_at: datetime