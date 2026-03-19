"""
This file handles authorization logic for admin routes.

It ensures that:
- The user is authenticated.
- The user has the correct role (admin or moderator).

This prevents regular users from accessing sensitive endpoints.
"""

from fastapi import Depends, HTTPException, status
from app.auth.service import get_current_user  # function that verifies Firebase JWT
from app.auth.schemas import UserOut  # schema representing authenticated user


def require_admin(current_user: UserOut = Depends(get_current_user)):
    """
    Dependency used to restrict routes to admin users only.

    If the current user is not an admin, access is denied.
    """

    if current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Admin privileges required."
        )

    return current_user


def require_moderator_or_admin(current_user: UserOut = Depends(get_current_user)):
    """
    Allows both moderators and admins to access certain routes.
    Useful for community moderation.
    """

    if current_user.role not in ["admin", "moderator"]:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Moderator or Admin privileges required."
        )

    return current_user