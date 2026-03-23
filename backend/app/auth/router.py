from fastapi import APIRouter, HTTPException, Request
from app.auth.schemas import TokenRequest
from app.auth.service import (
    verify_firebase_token_and_get_user,
    register_failed_attempt
)
from slowapi import Limiter
from slowapi.util import get_remote_address

limiter = Limiter(key_func=get_remote_address)

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post("/login")
@limiter.limit("5/minute")
async def login_with_firebase(
    payload: TokenRequest,
    request: Request,
):
    ip = request.client.host

    try:
        return verify_firebase_token_and_get_user(payload.id_token)

    except HTTPException as e:
        register_failed_attempt("unknown", ip)
        raise e

    except Exception:
        register_failed_attempt("unknown", ip)
        raise HTTPException(
            status_code=500,
            detail="Authentication failed"
        )


@router.get("/health")
def auth_health_check():
    return {"status": "auth service running"}