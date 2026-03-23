from fastapi import APIRouter, Depends
from app.issues.schemas import IssueCreate, IssueUpdate
from app.issues import service
from app.auth.service import get_current_user

router = APIRouter(prefix="/issues", tags=["Issues"])


@router.post("/")
def create_issue(data: IssueCreate, user=Depends(get_current_user)):
    return service.create_issue(user["sub"], data)


@router.put("/{issue_id}")
def update_issue(issue_id: str, data: IssueUpdate):
    return service.update_issue(issue_id, data)