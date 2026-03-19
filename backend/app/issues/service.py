# app/issues/service.py

from uuid import uuid4
from datetime import datetime
from supabase import Client


def create_issue(db: Client, user_id, data):
    issue = {
        "id": str(uuid4()),
        "user_id": str(user_id),
        "title": data.title,
        "description": data.description,
        "category": data.category,
        "status": "open",
        "created_at": datetime.utcnow().isoformat()
    }

    db.table("issue_reports").insert(issue).execute()
    return issue


def update_issue(db: Client, issue_id, data):
    db.table("issue_reports").update({
        "status": data.status,
        "admin_note": data.admin_note
    }).eq("id", str(issue_id)).execute()

    return {"message": "Issue updated"}