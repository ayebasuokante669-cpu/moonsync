"""
Admin routes for moderation and management.
All routes are protected by admin-level permissions.
"""

from fastapi import APIRouter, Depends
from uuid import UUID
from app.admin.permissions import require_admin
from app.admin.schemas import IssueReportOut, UpdateIssueStatus
from app.admin import service
from app.core.database import get_db

router = APIRouter(prefix="/admin", tags=["admin"])


@router.get("/stats")
def get_stats(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    """
    Returns platform-wide counts for the admin dashboard.
    """
    return service.get_stats(db)


@router.get("/issues", response_model=list[IssueReportOut])
def get_issues(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    """
    Get all issue reports.
    Only accessible by admins.
    """
    return service.get_all_issue_reports(db)


@router.put("/issues/{issue_id}")
def update_issue(
    issue_id: UUID,
    payload: UpdateIssueStatus,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    """
    Update issue status (resolve/reject).
    """
    service.update_issue_status(db, issue_id, payload.status)

    service.log_admin_action(
        db,
        admin_id=current_user.id,
        action_type="update_issue_status",
        target_table="issue_reports",
        target_id=issue_id
    )

    return {"message": "Issue updated successfully."}

"""
User management endpoints (e.g., banning/unbanning users, etc.).
"""
@router.get("/users")
def get_users(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_all_users(db)


@router.get("/users/{user_id}")
def get_user(
    user_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_user_by_id(db, user_id)


@router.post("/users/{user_id}/suspend")
def suspend_user(
    user_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    service.suspend_user(db, user_id)

    service.log_admin_action(
        db,
        admin_id=current_user.id,
        action_type="suspend_user",
        target_table="users",
        target_id=user_id
    )

    return {"message": "User suspended."}


@router.post("/users/{user_id}/ban")
def ban_user(
    user_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    service.ban_user(db, user_id)

    service.log_admin_action(
        db,
        admin_id=current_user.id,
        action_type="ban_user",
        target_table="users",
        target_id=user_id
    )

    return {"message": "User banned."}

""""
Password reset endpoint for admins to reset user passwords.
"""
@router.post("/users/{user_id}/reset-password")
def admin_reset_password(
    user_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):

    token = service.create_password_reset_token(db, user_id)

    service.log_admin_action(
        db,
        admin_id=current_user.id,
        action_type="reset_password",
        target_table="users",
        target_id=user_id
    )

    return {
        "message": "Password reset initiated.",
        "reset_token": token
    }

"""
Warning system endpoints for issuing warnings to users and overseeing them.
"""
@router.post("/users/{user_id}/warn")
def warn_user(
    user_id: UUID,
    message: str,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):

    service.warn_user(db, user_id, message)

    service.log_admin_action(
        db,
        admin_id=current_user.id,
        action_type="warn_user",
        target_table="users",
        target_id=user_id
    )

    return {"message": "User warned successfully."}

"""
Medical Verification endpoints for verifying medical documents submitted by professional users.
"""
@router.get("/medical-verifications")
def get_medical_verifications(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_medical_verifications(db)


@router.post("/medical-verifications/{verification_id}/approve")
def approve_medical_verification(
    verification_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):

    service.approve_medical_verification(db, verification_id)

    return {"message": "Medical verification approved."}


@router.post("/medical-verifications/{verification_id}/revoke")
def revoke_medical_verification(
    verification_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):

    service.revoke_medical_verification(db, verification_id)

    return {"message": "Verification revoked."}

"""
Articles management endpoints for overseeing user-submitted articles and content.
"""
@router.get("/articles")
def get_articles(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_all_articles(db)


@router.post("/articles")
def create_article(
    payload,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.create_article(db, payload)


@router.patch("/articles/{article_id}")
def update_article(
    article_id: UUID,
    payload,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.update_article(db, article_id, payload)


@router.delete("/articles/{article_id}")
def delete_article(
    article_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    service.delete_article(db, article_id)

    return {"message": "Article deleted."}

"""
Notification management endpoints for sending announcements and managing notifications to users.
"""
@router.get("/notifications")
def get_notifications(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_notifications(db)


@router.post("/send-notification")
def send_notification(
    payload,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.send_notification(db, payload)


@router.delete("/notifications/{notification_id}")
def delete_notification(
    notification_id: UUID,
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    service.delete_notification(db, notification_id)

    return {"message": "Notification deleted."}

"""
Analytics endpoints for providing insights and data to admins about platform usage, user behavior, etc. per day
"""
@router.get("/analytics/daily")
def daily_analytics(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_daily_analytics(db)


@router.get("/analytics/users")
def user_analytics(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_user_analytics(db)


@router.get("/analytics/engagement")
def engagement_analytics(
    db = Depends(get_db),
    current_user = Depends(require_admin)
):
    return service.get_engagement_analytics(db)

