"""
Authentication service logic.

Handles:
- Firebase token verification
- User retrieval / creation
"""

from fastapi import Depends, Header, HTTPException
from firebase_admin import auth as firebase_auth
from firebase_admin import initialize_app
from sqlalchemy.orm import Session

from app.core.database import get_db
from app.auth.schemas import UserResponse
from app.auth.roles import get_default_role
from app.users.models import User


# -------------------------------------------
# Firebase initialization
# -------------------------------------------

try:
    initialize_app()
except ValueError:
    pass


# -------------------------------------------
# Verify Firebase token and return user
# -------------------------------------------

def verify_firebase_token_and_get_user(id_token: str, db: Session) -> UserResponse:

    try:
        decoded_token = firebase_auth.verify_id_token(id_token)
    except Exception:
        raise HTTPException(status_code=401, detail="Invalid Firebase token")

    firebase_uid = decoded_token.get("uid")
    email = decoded_token.get("email")
    name = decoded_token.get("name")

    if not firebase_uid or not email:
        raise HTTPException(status_code=400, detail="Incomplete Firebase user data")

    # Check if user exists
    user = db.query(User).filter(User.firebase_uid == firebase_uid).first()

    if not user:
        user = User(
            firebase_uid=firebase_uid,
            email=email,
            name=name,
            role=get_default_role()
        )

        db.add(user)
        db.commit()
        db.refresh(user)

    return UserResponse(
        id=user.id,
        email=user.email,
        name=user.name,
        role=user.role
    )


# -------------------------------------------
# Dependency: Get current authenticated user
# -------------------------------------------

def get_current_user(
    authorization: str = Header(...),
    db: Session = Depends(get_db)
):

    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid Authorization header")

    token = authorization.split("Bearer ")[1]

    return verify_firebase_token_and_get_user(token, db)