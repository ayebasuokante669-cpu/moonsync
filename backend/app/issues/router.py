# app/issues/router.py

from fastapi import APIRouter, Depends
from app.issues.schemas import IssueCreate, IssueUpdate
from app.issues import service
from app.database import get_db
from app.auth.service import get_current_user

router = APIRouter(prefix="/issues", tags=["Issues"])


@router.post("/")
def create_issue(data: IssueCreate, db=Depends(get_db), user=Depends(get_current_user)):
    return service.create_issue(db, user["uid"], data)


@router.put("/{issue_id}")
def update_issue(issue_id: str, data: IssueUpdate, db=Depends(get_db)):
    return service.update_issue(db, issue_id, data)