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


class UpdateIssueStatus(BaseModel):
    status: str


# OPTIONAL (for better typing later)

class ArticleCreate(BaseModel):
    title: str
    content: str
    category: str


class NotificationCreate(BaseModel):
    title: str
    message: str