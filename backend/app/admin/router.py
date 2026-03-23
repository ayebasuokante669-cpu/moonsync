from fastapi import APIRouter, Depends
from uuid import UUID

from app.admin.permissions import require_admin
from app.admin.schemas import IssueReportOut, UpdateIssueStatus
from app.admin import service

router = APIRouter(prefix="/admin", tags=["admin"])


# =========================
# DASHBOARD
# =========================

@router.get("/stats")
def get_stats(current_user = Depends(require_admin)):
    return service.get_dashboard_stats()


# =========================
# ISSUES
# =========================

@router.get("/issues", response_model=list[IssueReportOut])
def get_issues(current_user = Depends(require_admin)):
    return service.get_all_issue_reports()


@router.put("/issues/{issue_id}")
def update_issue(issue_id: UUID, payload: UpdateIssueStatus, current_user = Depends(require_admin)):
    service.update_issue_status(issue_id, payload.status)

    service.log_admin_action(
        admin_id=current_user.id,
        action_type="update_issue_status",
        target_table="issue_reports",
        target_id=issue_id
    )

    return {"message": "Issue updated successfully."}


# =========================
# USERS
# =========================

@router.get("/users")
def get_users(current_user = Depends(require_admin)):
    return service.get_all_users()


@router.get("/users/{user_id}")
def get_user(user_id: UUID, current_user = Depends(require_admin)):
    return service.get_user_by_id(user_id)


@router.post("/users/{user_id}/suspend")
def suspend_user(user_id: UUID, current_user = Depends(require_admin)):
    service.suspend_user(user_id)

    service.log_admin_action(current_user.id, "suspend_user", "users", user_id)
    return {"message": "User suspended."}


@router.post("/users/{user_id}/ban")
def ban_user(user_id: UUID, current_user = Depends(require_admin)):
    service.ban_user(user_id)

    service.log_admin_action(current_user.id, "ban_user", "users", user_id)
    return {"message": "User banned."}


@router.post("/users/{user_id}/warn")
def warn_user(user_id: UUID, message: str, current_user = Depends(require_admin)):
    service.warn_user(user_id, message)

    service.log_admin_action(current_user.id, "warn_user", "users", user_id)
    return {"message": "User warned successfully."}


# =========================
# PASSWORD RESET
# =========================

@router.post("/users/{user_id}/reset-password")
def reset_password(user_id: UUID, current_user = Depends(require_admin)):
    token = service.create_password_reset_token(user_id)

    service.log_admin_action(current_user.id, "reset_password", "users", user_id)

    return {
        "message": "Password reset initiated.",
        "reset_token": token
    }


# =========================
# MEDICAL
# =========================

@router.get("/medical-verifications")
def get_medical(current_user = Depends(require_admin)):
    return service.get_medical_verifications()


@router.post("/medical-verifications/{id}/approve")
def approve_medical(id: UUID, current_user = Depends(require_admin)):
    service.approve_medical_verification(id)
    return {"message": "Approved"}


@router.post("/medical-verifications/{id}/revoke")
def revoke_medical(id: UUID, current_user = Depends(require_admin)):
    service.revoke_medical_verification(id)
    return {"message": "Revoked"}


# =========================
# ARTICLES
# =========================

@router.get("/articles")
def get_articles(current_user = Depends(require_admin)):
    return service.get_all_articles()


@router.post("/articles")
def create_article(payload, current_user = Depends(require_admin)):
    return service.create_article(payload)


@router.patch("/articles/{id}")
def update_article(id: UUID, payload, current_user = Depends(require_admin)):
    return service.update_article(id, payload)


@router.delete("/articles/{id}")
def delete_article(id: UUID, current_user = Depends(require_admin)):
    service.delete_article(id)
    return {"message": "Deleted"}


# =========================
# NOTIFICATIONS
# =========================

@router.get("/notifications")
def get_notifications(current_user = Depends(require_admin)):
    return service.get_notifications()


@router.post("/notifications")
def send_notification(payload, current_user = Depends(require_admin)):
    return service.send_notification(payload)


@router.delete("/notifications/{id}")
def delete_notification(id: UUID, current_user = Depends(require_admin)):
    service.delete_notification(id)
    return {"message": "Deleted"}