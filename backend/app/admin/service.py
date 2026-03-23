from uuid import UUID
from datetime import datetime
import secrets

from app.core.supabase import supabase


# =========================
# ISSUE REPORTS
# =========================

def get_all_issue_reports():
    return supabase.table("issue_reports").select("*").execute()


def update_issue_status(issue_id: UUID, status: str):
    return supabase.table("issue_reports") \
        .update({"status": status}) \
        .eq("id", str(issue_id)) \
        .execute()


# =========================
# ADMIN LOGGING
# =========================

def log_admin_action(admin_id: UUID, action_type: str, target_table: str, target_id: UUID):
    return supabase.table("admin_actions").insert({
        "admin_id": str(admin_id),
        "action_type": action_type,
        "target_table": target_table,
        "target_id": str(target_id),
        "created_at": datetime.utcnow().isoformat()
    }).execute()


# =========================
# USERS
# =========================

def get_all_users():
    return supabase.table("users").select("*").execute()


def get_user_by_id(user_id: UUID):
    return supabase.table("users") \
        .select("*") \
        .eq("id", str(user_id)) \
        .single() \
        .execute()


def suspend_user(user_id: UUID):
    return supabase.table("users") \
        .update({"status": "suspended"}) \
        .eq("id", str(user_id)) \
        .execute()


def ban_user(user_id: UUID):
    return supabase.table("users") \
        .update({"status": "banned"}) \
        .eq("id", str(user_id)) \
        .execute()


def warn_user(user_id: UUID, message: str):
    return supabase.table("user_warnings").insert({
        "user_id": str(user_id),
        "message": message,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


# =========================
# PASSWORD RESET
# =========================

def create_password_reset_token(user_id: UUID):
    token = secrets.token_urlsafe(32)

    supabase.table("password_reset_tokens").insert({
        "user_id": str(user_id),
        "token": token,
        "expires_at": datetime.utcnow().isoformat(),
        "used": False
    }).execute()

    return token


# =========================
# MEDICAL VERIFICATION
# =========================

def get_medical_verifications():
    return supabase.table("doctor_verifications") \
        .select("*") \
        .eq("status", "pending") \
        .execute()


def approve_medical_verification(verification_id: UUID):
    return supabase.table("doctor_verifications") \
        .update({"status": "approved"}) \
        .eq("id", str(verification_id)) \
        .execute()


def revoke_medical_verification(verification_id: UUID):
    return supabase.table("doctor_verifications") \
        .update({"status": "revoked"}) \
        .eq("id", str(verification_id)) \
        .execute()


# =========================
# ARTICLES
# =========================

def get_all_articles():
    return supabase.table("articles").select("*").execute()


def create_article(payload):
    return supabase.table("articles").insert({
        "title": payload.title,
        "content": payload.content,
        "category": payload.category,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


def update_article(article_id: UUID, payload):
    return supabase.table("articles") \
        .update({
            "title": payload.title,
            "content": payload.content,
            "category": payload.category
        }) \
        .eq("id", str(article_id)) \
        .execute()


def delete_article(article_id: UUID):
    return supabase.table("articles") \
        .delete() \
        .eq("id", str(article_id)) \
        .execute()


# =========================
# NOTIFICATIONS
# =========================

def get_notifications():
    return supabase.table("scheduled_notifications").select("*").execute()


def send_notification(payload):
    return supabase.table("scheduled_notifications").insert({
        "title": payload.title,
        "message": payload.message,
        "created_at": datetime.utcnow().isoformat()
    }).execute()


def delete_notification(notification_id: UUID):
    return supabase.table("scheduled_notifications") \
        .delete() \
        .eq("id", str(notification_id)) \
        .execute()


# =========================
# ANALYTICS
# =========================

def get_dashboard_stats():
    users = supabase.table("users").select("*", count="exact").execute()
    articles = supabase.table("articles").select("*", count="exact").execute()

    return {
        "total_users": users.count,
        "total_articles": articles.count
    }