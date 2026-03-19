"""
Pydantic models used by admin routes.
These define the structure of data sent to and from the admin endpoints.
"""

from pydantic import BaseModel
from uuid import UUID
from datetime import datetime


class IssueReportOut(BaseModel):
    id: UUID
    user_id: UUID
    title: str
    description: str
    status: str
    created_at: datetime


class AdminActionOut(BaseModel):
    id: UUID
    admin_id: UUID
    action_type: str
    target_table: str
    target_id: UUID
    created_at: datetime


class UpdateIssueStatus(BaseModel):
    status: str  # open, resolved, rejected