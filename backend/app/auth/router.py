"""
Authentication routes.
This file defines all auth-related API endpoints.
"""

from fastapi import APIRouter, Depends, HTTPException, status

from app.auth.schemas import TokenRequest, UserResponse
from app.auth.service import verify_firebase_token_and_get_user

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post("/login", response_model=UserResponse)
async def login_with_firebase(payload: TokenRequest):
    """
    Login endpoint using Firebase authentication.

    Flow:
    1. Client sends Firebase ID token
    2. Backend verifies token via Firebase Admin SDK
    3. Backend fetches or creates user in database
    4. User info is returned to the client
    """

    try:
        user = verify_firebase_token_and_get_user(payload.id_token)
        return user

    except ValueError as e:
        # Raised when token is invalid or expired
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=str(e)
        )

    except Exception:
        # Catch-all for unexpected server issues
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Authentication failed"
        )


@router.get("/health")
def auth_health_check():
    """
    Simple health check for auth routes.
    Useful for testing if auth module is reachable.
    """
    return {"status": "auth service running"}