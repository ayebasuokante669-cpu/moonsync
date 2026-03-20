"""
Contains the business logic for admin operations.
Database queries and admin actions are handled here.
"""

from uuid import UUID
from datetime import datetime
import secrets

from app.admin.permissions import require_admin
from app.community import service
from app.core.database import get_db
from fastapi import Depends, HTTPException
from app.admin import service
from app.auth.service import get_current_user



def get_all_issue_reports(db):
    """
    Fetch all issue reports from database.
    """

    return db.table("issue_reports").select("*").execute()


def update_issue_status(db, issue_id: UUID, status: str):
    """
    Update the status of an issue report.
    """

    return db.table("issue_reports") \
        .update({"status": status}) \
        .eq("id", str(issue_id)) \
        .execute()


def log_admin_action(db, admin_id: UUID, action_type: str, target_table: str, target_id: UUID):
    """
    Records any action taken by an admin.
    Useful for auditing and tracking moderation.
    """

    return db.table("admin_actions").insert({
        "admin_id": str(admin_id),
        "action_type": action_type,
        "target_table": target_table,
        "target_id": str(target_id),
        "created_at": datetime.utcnow().isoformat()
    }).execute()

"""
User management services.
"""
def get_all_users(db):
    return db.table("users").select("*").execute()


def get_user_by_id(db, user_id: UUID):
    return db.table("users").select("*").eq("id", str(user_id)).single().execute()


def suspend_user(db, user_id: UUID):
    return db.table("users") \
        .update({"status": "suspended"}) \
        .eq("id", str(user_id)) \
        .execute()


def ban_user(db, user_id: UUID):
    return db.table("users") \
        .update({"status": "banned"}) \
        .eq("id", str(user_id)) \
        .execute()

"""
Warning functions for user management (e.g., sending warning messages to users, etc.).
"""
def warn_user(db, user_id: UUID, message: str):

    db.table("user_warnings").insert({
        "user_id": str(user_id),
        "message": message,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


"""
Passowrd reset token management (e.g., generating tokens, validating tokens, etc.).
"""
def create_password_reset_token(db, user_id: UUID):

    token = secrets.token_urlsafe(32)

    db.table("password_reset_tokens").insert({
        "user_id": str(user_id),
        "token": token,
        "expires_at": datetime.utcnow().isoformat(),
        "used": False
    }).execute()

    return token

"""
Medical Verification 
"""
def get_medical_verifications(db):

    return db.table("doctor_verifications") \
        .select("*") \
        .eq("status", "pending") \
        .execute()


def approve_medical_verification(db, verification_id: UUID):

    return db.table("doctor_verifications") \
        .update({"status": "approved"}) \
        .eq("id", str(verification_id)) \
        .execute()


def revoke_medical_verification(db, verification_id: UUID):

    return db.table("doctor_verifications") \
        .update({"status": "revoked"}) \
        .eq("id", str(verification_id)) \
        .execute()

"""
Articles Management
"""
def get_all_articles(db):

    return db.table("articles") \
        .select("*") \
        .execute()


def create_article(db, payload):

    return db.table("articles").insert({
        "title": payload.title,
        "content": payload.content,
        "category": payload.category,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


def update_article(db, article_id: UUID, payload):

    return db.table("articles") \
        .update({
            "title": payload.title,
            "content": payload.content,
            "category": payload.category
        }) \
        .eq("id", str(article_id)) \
        .execute()


def delete_article(db, article_id: UUID):

    return db.table("articles") \
        .delete() \
        .eq("id", str(article_id)) \
        .execute()


"""
Notificaiton management services for sending notifications to users, etc.
"""
def get_notifications(db):

    return db.table("scheduled_notifications") \
        .select("*") \
        .execute()


def send_notification(db, payload):

    return db.table("scheduled_notifications").insert({
        "title": payload.title,
        "message": payload.message,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


def delete_notification(db, notification_id: UUID):

    return db.table("scheduled_notifications") \
        .delete() \
        .eq("id", str(notification_id)) \
        .execute()

"""
Require admin role for all admin services.
This is a simple wrapper to ensure that only admins can call these functions.
"""
def require_admin(user = Depends(get_current_user)):

    if user.role != "admin":
        raise HTTPException(
            status_code=403,
            detail="Admin privileges required"
        )
    if not user:
        raise HTTPException(status_code=401, detail="User not found")

    return user


