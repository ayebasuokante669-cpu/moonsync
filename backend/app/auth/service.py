from fastapi import Header, HTTPException
from firebase_admin import auth as firebase_auth, initialize_app
from datetime import datetime, timedelta

from app.core.supabase import supabase
from app.core.security import create_access_token, verify_token
from app.auth.roles import get_default_role


# -------------------------------------------
# Firebase initialization
# -------------------------------------------

try:
    initialize_app()
except ValueError:
    pass


# -------------------------------------------
# SECURITY TRACKERS
# -------------------------------------------

failed_attempts = {}
blocked_users = {}
ip_attempts = {}

MAX_ATTEMPTS = 5
BLOCK_DURATION = timedelta(minutes=10)


# -------------------------------------------
# SECURITY FUNCTIONS
# -------------------------------------------

def is_user_blocked(email: str):
    if email in blocked_users:
        if datetime.now() < blocked_users[email]:
            return True
        else:
            del blocked_users[email]
    return False


def register_failed_attempt(email: str, ip: str):
    failed_attempts[email] = failed_attempts.get(email, 0) + 1
    ip_attempts[ip] = ip_attempts.get(ip, 0) + 1

    print(f"[LOGIN FAIL] {email} from {ip} (Attempts: {failed_attempts[email]})")

    if failed_attempts[email] >= MAX_ATTEMPTS:
        blocked_users[email] = datetime.now() + BLOCK_DURATION
        failed_attempts[email] = 0
        print(f"[BLOCKED USER] {email} until {blocked_users[email]}")


def reset_attempts(email: str):
    failed_attempts.pop(email, None)


# -------------------------------------------
# VERIFY TOKEN + USER HANDLING
# -------------------------------------------

def verify_firebase_token_and_get_user(id_token: str) -> dict:

    try:
        decoded_token = firebase_auth.verify_id_token(id_token)
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid Firebase token")

    firebase_uid = decoded_token.get("uid")
    email = decoded_token.get("email")
    name = decoded_token.get("name")

    if not firebase_uid or not email:
        raise HTTPException(status_code=400, detail="Incomplete Firebase user data")

    if is_user_blocked(email):
        raise HTTPException(
            status_code=403,
            detail="Too many failed attempts. Try again later."
        )

    # Supabase DB lookup
    result = supabase.table("users").select("*").eq("firebase_uid", firebase_uid).execute()
    user = result.data[0] if result.data else None

    if not user:
        # Insert new user
        insert_result = supabase.table("users").insert({
            "firebase_uid": firebase_uid,
            "email": email,
            "name": name,
            "role": get_default_role()
        }).execute()
        user = insert_result.data[0]

    reset_attempts(email)

    access_token = create_access_token({
        "sub": str(user["id"]),
        "email": user["email"],
        "role": user["role"]
    })

    return {
        "id": user["id"],
        "email": user["email"],
        "name": user.get("name"),
        "role": user["role"],
        "access_token": access_token
    }


# -------------------------------------------
# CURRENT USER DEPENDENCY
# -------------------------------------------

def get_current_user(
    authorization: str = Header(...)
):
    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid Authorization header")

    token = authorization.split("Bearer ")[1]
    payload = verify_token(token)

    return payload